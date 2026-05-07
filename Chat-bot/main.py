from __future__ import annotations

import json
import logging
import os
import re
from datetime import date
from typing import Any, Literal, Optional

import httpx
from fastapi import FastAPI
from pydantic import BaseModel, Field

LOG = logging.getLogger("fixlab.chatbot")

GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
MAX_CHARS_PER_MESSAGE = 12000

# Último resultado de una llamada a Gemini (para /health; no incluye la API key).
_LAST_GEMINI_DIAG: dict[str, Any] = {}

SYSTEM_PROMPT = (
    "Eres el asistente virtual de FixLab, una tienda/taller en línea. "
    "Responde siempre en español de forma clara y breve. "
    "FixLab usa Wompi para pagos. Los pedidos y el taller se gestionan desde la cuenta (dashboard) "
    "y áreas de administración. "
    "Usa el bloque de contexto cuando venga: pantalla del usuario y datos reales de cuenta si los hay. "
    "No inventes datos de pedidos ni políticas. "
    "Enlaces Markdown solo con rutas que empiecen por / (ej. [/productos](/productos)); máximo uno o dos por respuesta. "
    "No escribas el encabezado \"## Contexto de esta petición\" ni metadatos del sistema en la respuesta."
)


def _navigation_hint_for_role(rol: str | None) -> str:
    r = (rol or "CLIENTE").upper()
    if r == "ADMIN":
        return (
            "Perfil: administrador. Rutas útiles: [/admin/productos](/admin/productos), "
            "[/admin/pedidos](/admin/pedidos), [/admin/taller/lista](/admin/taller/lista), "
            "[/admin/usuarios](/admin/usuarios). Catálogo público: [/productos](/productos)."
        )
    if r == "RECEPCIONISTA":
        return (
            "Perfil: recepcionista. Rutas: [/admin/recepcion](/admin/recepcion), "
            "[/admin/taller/lista](/admin/taller/lista), [/reparaciones](/reparaciones), "
            "[/productos](/productos)."
        )
    if r == "TECNICO":
        return (
            "Perfil: técnico. Rutas: [/admin/taller/lista](/admin/taller/lista), "
            "[/admin/taller/gestion](/admin/taller/gestion), [/productos](/productos)."
        )
    return (
        "Perfil: cliente. Rutas típicas: [/productos](/productos), [/carrito](/carrito), "
        "[/dashboard](/dashboard), [/reparaciones](/reparaciones)."
    )


def _truncate(s: str, max_len: int = MAX_CHARS_PER_MESSAGE) -> str:
    if len(s) <= max_len:
        return s
    return s[:max_len]


def _generation_config_for_model(model: str) -> dict[str, Any] | None:
    """Reduce latencia y evita respuestas sin texto útil en modelos 2.5 con thinking."""
    if os.getenv("GEMINI_SKIP_GENERATION_CONFIG", "").lower() in ("1", "true", "yes"):
        return None
    m = model.lower()
    cfg: dict[str, Any] = {"maxOutputTokens": 1024}
    if "2.5" in m:
        cfg["thinkingConfig"] = {"thinkingBudget": 0}
    return cfg


def _clear_gemini_diag() -> None:
    global _LAST_GEMINI_DIAG
    _LAST_GEMINI_DIAG = {"ok": True, "http_status": 200, "error_preview": ""}


def _set_gemini_diag(ok: bool, http_status: int, snippet: str, note: str = "") -> None:
    global _LAST_GEMINI_DIAG
    _LAST_GEMINI_DIAG = {
        "ok": ok,
        "http_status": http_status,
        "error_preview": (snippet or "")[:600],
        "note": note,
    }


def _build_system_text(usuario_rol: str | None, contexto_turno: str | None) -> str:
    system_text = SYSTEM_PROMPT + "\n\n" + _navigation_hint_for_role(usuario_rol)
    if contexto_turno and contexto_turno.strip():
        system_text += (
            "\n\n## Contexto de esta petición (datos reales enviados por la app; úsalos en la respuesta)\n"
            + contexto_turno.strip()
        )
    return system_text


def _extract_text_from_candidate(candidate: dict[str, Any]) -> str | None:
    """
    Une todas las partes con texto visible. Gemini 2.x puede devolver varias parts
    (p. ej. bloques internos / thinking) donde la primera no es la respuesta al usuario.
    """
    parts = (candidate.get("content") or {}).get("parts") or []
    chunks: list[str] = []
    for part in parts:
        if not isinstance(part, dict):
            continue
        if part.get("thought") is True:
            continue
        t = part.get("text")
        if isinstance(t, str) and t.strip():
            chunks.append(t.strip())
    if not chunks:
        return None
    return "\n\n".join(chunks).strip()


def _parse_gemini_response(data: dict[str, Any]) -> tuple[str | None, str]:
    """Devuelve (texto, motivo_debug). Si hay texto, motivo es ''."""
    pf = data.get("promptFeedback") or {}
    if pf.get("blockReason"):
        return None, f"promptFeedback.blockReason={pf.get('blockReason')}"

    candidates = data.get("candidates")
    if not candidates or not isinstance(candidates, list):
        err = data.get("error") if isinstance(data.get("error"), dict) else {}
        msg = err.get("message", "")
        return None, f"sin candidates error={msg[:200]!s}" if msg else "sin candidates"

    first = candidates[0]
    reason = first.get("finishReason") or ""

    if reason == "SAFETY":
        return None, "finishReason=SAFETY"

    text = _extract_text_from_candidate(first)
    if text:
        return text, ""

    return None, f"sin texto en parts finishReason={reason!s}"


def _post_gemini(
    client: httpx.Client,
    api_key: str,
    model: str,
    body: dict[str, Any],
    *,
    api_key_in_query: bool = False,
) -> tuple[dict[str, Any] | None, int, str]:
    url = f"{GEMINI_BASE_URL}/v1beta/models/{model}:generateContent"
    headers: dict[str, str] = {"Content-Type": "application/json"}
    params: dict[str, str] | None = None
    if api_key_in_query:
        params = {"key": api_key.strip()}
    else:
        headers["x-goog-api-key"] = api_key.strip()
    try:
        r = client.post(
            url,
            headers=headers,
            params=params,
            json=body,
        )
        snippet = (r.text or "")[:800]
        if r.status_code != 200:
            return None, r.status_code, snippet
        try:
            return r.json(), r.status_code, ""
        except json.JSONDecodeError:
            return None, r.status_code, snippet[:800]
    except httpx.HTTPError as e:
        return None, -1, str(e)[:400]


def _merge_system_into_first_user_turn(
    system_text: str,
    contents: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    """Fallback si la API rechaza systemInstruction: anteponer instrucciones al primer turno user."""
    merged = json.loads(json.dumps(contents))
    if not merged:
        return [{"role": "user", "parts": [{"text": system_text}]}]
    first = merged[0]
    role = first.get("role")
    parts = first.get("parts") or []
    if role != "user" or not parts or not isinstance(parts[0], dict):
        return [{"role": "user", "parts": [{"text": system_text}]}, *merged]
    orig = (parts[0].get("text") or "").strip()
    parts[0]["text"] = system_text + "\n\n---\n\n" + orig if orig else system_text
    return merged


def _gemini_generate(
    api_key: str,
    model: str,
    usuario_rol: str | None,
    contexto_turno: str | None,
    history: list[ChatHistoryItem],
) -> str | None:
    """Llama a la API REST de Gemini (misma forma que fixlab-api Java). Devuelve texto o None si falla."""
    contents: list[dict[str, Any]] = []
    for item in history:
        role = "user" if item.role == "USER" else "model"
        text = _truncate(item.text.strip())
        if not text:
            continue
        contents.append({"role": role, "parts": [{"text": text}]})

    if not contents:
        _set_gemini_diag(False, 0, "", "contents vacío tras historial")
        return None

    system_text = _build_system_text(usuario_rol, contexto_turno)
    gen_cfg = _generation_config_for_model(model)

    base_primary: dict[str, Any] = {
        "systemInstruction": {"parts": [{"text": system_text}]},
        "contents": contents,
    }
    base_fallback: dict[str, Any] = {
        "contents": _merge_system_into_first_user_turn(system_text, contents),
    }

    attempt_bodies: list[tuple[str, dict[str, Any]]] = []
    if gen_cfg:
        attempt_bodies.append(
            ("primary+generationConfig", {**base_primary, "generationConfig": gen_cfg}),
        )
        attempt_bodies.append(
            ("fallback_merge+generationConfig", {**base_fallback, "generationConfig": gen_cfg}),
        )
    attempt_bodies.append(("primary", dict(base_primary)))
    attempt_bodies.append(("fallback_merge", dict(base_fallback)))

    with httpx.Client(timeout=90.0) as client:
        for label, body in attempt_bodies:
            for query_key in (False, True):
                data, status, err_snippet = _post_gemini(
                    client,
                    api_key,
                    model,
                    body,
                    api_key_in_query=query_key,
                )
                auth_note = "query_key=1" if query_key else "header_key"

                if status != 200 or data is None:
                    LOG.warning(
                        "Gemini [%s/%s] modelo=%s status=%s snippet=%s",
                        label,
                        auth_note,
                        model,
                        status,
                        err_snippet[:400] if err_snippet else "",
                    )
                    _set_gemini_diag(False, status, err_snippet, f"{label} {auth_note}")
                    # 429: cada reintento cuenta contra la cuota free tier; no seguir.
                    if status == 429:
                        LOG.warning(
                            "Gemini 429 (cuota/RPM): se detienen reintentos para este mensaje. "
                            "Espera lo que indica Google o revisa facturación/plan."
                        )
                        return None
                    continue

                text_ok, dbg = _parse_gemini_response(data)
                if text_ok:
                    _clear_gemini_diag()
                    LOG.debug("Gemini OK [%s/%s]", label, auth_note)
                    return text_ok

                LOG.warning(
                    "Gemini [%s/%s] parse: %s",
                    label,
                    auth_note,
                    dbg,
                )
                _set_gemini_diag(False, status, dbg, f"{label} {auth_note}")

    return None


class ChatHistoryItem(BaseModel):
    role: Literal["USER", "BOT"]
    text: str = Field(min_length=1, max_length=4000)


class ChatReplyRequest(BaseModel):
    userText: str = Field(min_length=1, max_length=4000)
    usuarioRol: Optional[str] = Field(default=None, max_length=30)
    contextoTurno: Optional[str] = Field(default=None, max_length=4000)
    history: list[ChatHistoryItem] = Field(default_factory=list, max_length=50)


class ChatReplyResponse(BaseModel):
    text: str
    source: Literal["IA", "FAQ"] = "IA"


app = FastAPI(title="FixLab Chatbot (internal)", version="0.1.0")


def _normalize(s: str) -> str:
    s = s.lower().strip()
    s = re.sub(r"\s+", " ", s)
    return s


def _fecha_hoy_es_servidor() -> str:
    """Texto legible según la fecha del servidor donde corre el microservicio."""
    d = date.today()
    dias = (
        "lunes",
        "martes",
        "miércoles",
        "jueves",
        "viernes",
        "sábado",
        "domingo",
    )
    meses = (
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "septiembre",
        "octubre",
        "noviembre",
        "diciembre",
    )
    return f"{dias[d.weekday()]}, {d.day} de {meses[d.month - 1]} de {d.year}"


def _rule_based_reply(user_text: str, contexto_turno: str | None) -> str:
    n = _normalize(user_text)
    ruta = ""
    if contexto_turno:
        m = re.search(r"Ruta en la app web:\s*(.+)", contexto_turno)
        if m:
            ruta = m.group(1).strip()
            if len(ruta) > 200:
                ruta = ruta[:200]

    pantalla = f"Veo que estás en **{ruta}**. " if ruta else ""

    if re.search(r"\b(hola|buenas|hey|hi)\b", n):
        return (
            pantalla
            + "¡Hola! Puedo ayudarte con pedidos, pagos, envíos, productos o cuenta. "
            + "Si quieres, empieza por [/productos](/productos)."
        )
    if re.search(r"\b(gracias|thank)\b", n):
        return "¡Con gusto! Si necesitas algo más, aquí estaré."
    if re.search(r"\b(qu[eé]|que)\s+es\s+fixlab\b", n) or (
        "fixlab" in n and len(n) < 40
    ):
        return (
            pantalla
            + "FixLab es la plataforma de esta tienda y taller en línea: catálogo, pedidos con pagos (Wompi), "
            + "tu cuenta en [/dashboard](/dashboard) y seguimiento de reparaciones en [/reparaciones](/reparaciones)."
        )
    # Fecha / calendario (preguntas generales fuera del FAQ de tienda)
    if re.search(
        r"\b("
        r"que\s+dia\s+es\s+hoy|"
        r"qu[eé]\s+d[ií]a\s+es(\s+hoy)?|"
        r"fecha\s+de\s+hoy|"
        r"d[ií]a\s+de\s+hoy|"
        r"what\s+day\s+is\s+today|"
        r"today'?s?\s+date"
        r")\b",
        n,
    ):
        return (
            pantalla
            + f"Hoy es **{_fecha_hoy_es_servidor()}** "
            + "(fecha según el servidor del chatbot; puede diferir de tu zona horaria). "
            + "Para FixLab: pedidos, pagos o [/productos](/productos)."
        )
    if re.search(r"(pedido|orden|estado del pedido|mis compras)", n):
        return "Revisa tus pedidos en [/dashboard](/dashboard). Si aún no has comprado, ve a [/productos](/productos)."
    if re.search(r"(pago|pagar|wompi|tarjeta|checkout|transaccion)", n):
        return pantalla + "Los pagos se procesan con Wompi desde el carrito. Ve a [/carrito](/carrito) para continuar."
    if re.search(r"(envio|envío|domicilio|entrega)", n):
        return "Los tiempos y costos dependen de la zona. Luego del pago, el estado lo puedes ver en [/dashboard](/dashboard)."
    if re.search(r"(producto|catalogo|catálogo|comprar|tienda)", n):
        return "Aquí está el catálogo: [/productos](/productos). Puedes añadir al [/carrito](/carrito)."
    if re.search(r"(cuenta|registro|registrarme|contraseña|password|clave)", n):
        return "Cuenta: [/login](/login), [/register](/register), [/recuperar-password](/recuperar-password)."

    return (
        pantalla
        + "No tengo una respuesta exacta. Intenta con: pedido, pago, envío, productos o cuenta. "
        + "También puedes ir a [/home](/home)."
    )


@app.get("/health")
def health() -> dict[str, Any]:
    """Comprueba servicio y lectura de entorno (sin exponer la API key)."""
    mode = os.getenv("FIXLAB_CHAT_MODE", "rules").lower().strip()
    raw_key = (
        os.getenv("GEMINI_API_KEY", "").strip()
        or os.getenv("GOOGLE_API_KEY", "").strip()
    )
    model = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite").strip()
    effective = mode
    if mode == "auto":
        effective = "gemini" if raw_key else "rules"
    out: dict[str, Any] = {
        "ok": True,
        "fixlab_chat_mode": mode,
        "effective_mode": effective,
        "gemini_key_configured": bool(raw_key),
        "gemini_model": model,
    }
    if _LAST_GEMINI_DIAG:
        out["last_gemini"] = dict(_LAST_GEMINI_DIAG)
    return out


@app.post("/reply", response_model=ChatReplyResponse)
def reply(req: ChatReplyRequest) -> ChatReplyResponse:
    """
    Endpoint interno consumido por fixlab-api.
    - No autentica (se recomienda que SOLO sea accesible desde la red interna).
    - Devuelve una respuesta corta estilo asistente FixLab.
    """
    mode = os.getenv("FIXLAB_CHAT_MODE", "rules").lower().strip()
    api_key = (
        os.getenv("GEMINI_API_KEY", "").strip()
        or os.getenv("GOOGLE_API_KEY", "").strip()
    )
    model = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite").strip()

    if mode == "auto":
        mode = "gemini" if api_key else "rules"

    if mode == "gemini":
        if api_key:
            hist = list(req.history)
            if not hist:
                hist = [ChatHistoryItem(role="USER", text=req.userText)]
            gemini_text = _gemini_generate(
                api_key,
                model,
                req.usuarioRol,
                req.contextoTurno,
                hist,
            )
            if gemini_text:
                return ChatReplyResponse(text=gemini_text, source="IA")
        text = _rule_based_reply(req.userText, req.contextoTurno)
        return ChatReplyResponse(text=text, source="FAQ")

    if mode == "rules":
        text = _rule_based_reply(req.userText, req.contextoTurno)
        return ChatReplyResponse(text=text, source="IA")

    text = _rule_based_reply(req.userText, req.contextoTurno)
    return ChatReplyResponse(text=text, source="IA")

