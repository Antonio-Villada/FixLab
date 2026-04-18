package com.software.fixlab.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.fixlab.entity.ChatMensaje;
import com.software.fixlab.entity.ChatRol;
import com.software.fixlab.entity.RolUsuario;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class GeminiChatClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";
    private static final int MAX_CHARS_PER_MESSAGE = 12000;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT =
            "Eres el asistente virtual de FixLab, una tienda/taller en línea. "
                    + "Responde siempre en español de forma clara y breve. "
                    + "FixLab usa Wompi para pagos. Los pedidos y el taller se gestionan desde la cuenta (dashboard) "
                    + "y áreas de administración. "
                    + "Usa SIEMPRE el bloque \"Contexto de esta petición\" cuando venga: resume en qué pantalla está "
                    + "el usuario y qué datos reales tiene su cuenta (pedidos/reparaciones). "
                    + "Si el usuario pregunta por \"esta página\", \"aquí\" o \"lo que veo\", alude explícitamente a esa ruta o datos. "
                    + "Si no hay datos en el contexto para algo que preguntan, dilo y orienta a la sección correcta. "
                    + "Si no sabes algo específico del usuario (número de pedido, estado real), dilo y orienta a revisar "
                    + "su cuenta o contactar soporte. "
                    + "No inventes datos de pedidos ni políticas que no conozcas. "
                    + "Cuando indiques una página de la app, añade un enlace Markdown con ruta interna que empiece por / "
                    + "(ej. [Ver productos](/productos)). No uses URLs externas ni javascript:. "
                    + "Máximo uno o dos enlaces por respuesta, solo si ayudan al usuario. "
                    + "Nunca escribas en tu respuesta el encabezado \"## Contexto de esta petición\" ni un resumen "
                    + "literal de ese bloque (ni listar que no hay datos de cuenta); el usuario no debe ver metadatos "
                    + "del sistema: usa el contexto solo para razonar y responde en lenguaje natural directo.";


    public GeminiChatClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash-lite}")
    private String model;

    @Value("${gemini.max-history-messages:24}")
    private int maxHistoryMessages;

    @PostConstruct
    void logEstadoGemini() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn(
                    "Gemini desactivado: sin GEMINI_API_KEY (o gemini.api-key vacío). "
                            + "El chat usará solo respuestas de ayuda rápida (FAQ).");
        } else {
            log.info("Gemini activo; modelo={}", model);
        }
    }

    /**
     * Genera la respuesta del modelo a partir del historial completo del usuario (orden ascendente por fecha).
     * Si no hay clave, falla la petición o la respuesta está bloqueada, devuelve vacío para usar respaldo local.
     */
    /**
     * @param contextoTurno Texto libre (pantalla actual, carrito, resumen de cuenta) solo para esta respuesta;
     *                        no se persiste en el historial del chat.
     */
    public Optional<String> generateReply(
            List<ChatMensaje> historyOrderedAsc,
            RolUsuario rolUsuario,
            String contextoTurno) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        if (historyOrderedAsc == null || historyOrderedAsc.isEmpty()) {
            return Optional.empty();
        }

        List<ChatMensaje> slice = sliceHistory(historyOrderedAsc);
        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMensaje m : slice) {
            String role = m.getRol() == ChatRol.USER ? "user" : "model";
            String text = truncate(m.getTexto());
            if (text.isBlank()) {
                continue;
            }
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", text))
            ));
        }

        if (contents.isEmpty()) {
            return Optional.empty();
        }

        String systemText = SYSTEM_PROMPT + "\n\n" + navigationHintForRole(rolUsuario);
        if (contextoTurno != null && !contextoTurno.isBlank()) {
            systemText += "\n\n## Contexto de esta petición (datos reales enviados por la app; úsalos en la respuesta)\n"
                    + contextoTurno.trim();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemText))
        ));
        body.put("contents", contents);

        String path = "/v1beta/models/" + model + ":generateContent";

        try {
            String raw = restClient.post()
                    .uri(path)
                    .header("x-goog-api-key", apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) {
                log.warn("Gemini returned empty body");
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(raw);

            JsonNode promptFeedback = root.path("promptFeedback");
            if (promptFeedback.hasNonNull("blockReason")) {
                log.warn("Gemini blocked prompt: {}", promptFeedback.get("blockReason").asText());
                return Optional.empty();
            }

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.warn(
                        "Gemini sin candidates (revisa cuota o contenido). promptFeedback={}",
                        root.path("promptFeedback").toString());
                return Optional.empty();
            }

            JsonNode first = candidates.get(0);
            if ("SAFETY".equals(first.path("finishReason").asText())) {
                log.warn("Gemini finishReason SAFETY");
                return Optional.empty();
            }

            JsonNode parts = first.path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return Optional.empty();
            }

            String text = parts.get(0).path("text").asText("").trim();
            if (text.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(text);
        } catch (JsonProcessingException e) {
            log.warn("Gemini: respuesta JSON inválida: {}", e.getMessage());
            return Optional.empty();
        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            String snippet = errorBody != null && errorBody.length() > 800
                    ? errorBody.substring(0, 800) + "…"
                    : errorBody;
            if (snippet != null && snippet.toLowerCase(Locale.ROOT).contains("leaked")) {
                log.warn(
                        "Gemini: Google invalidó la API key (reportada como filtrada). Crea una NUEVA en "
                                + "https://aistudio.google.com/apikey , revoca la antigua y actualiza gemini.api-key "
                                + "o GEMINI_API_KEY. No compartas la clave en chats, capturas ni repositorios.");
            } else {
                log.warn(
                        "Gemini API rechazó la petición: status={} — {}. Revisa clave, modelo ({}) y cuota en Google AI Studio.",
                        e.getStatusCode().value(),
                        snippet != null ? snippet : e.getMessage(),
                        model);
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Gemini error de red/cliente: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String navigationHintForRole(RolUsuario rol) {
        if (rol == null) {
            rol = RolUsuario.CLIENTE;
        }
        return switch (rol) {
            case ADMIN -> "Perfil del usuario: administrador. Rutas útiles en respuestas cuando encaje: "
                    + "[Productos (admin)](/admin/productos), [Pedidos](/admin/pedidos), "
                    + "[Categorías](/admin/categorias), [Tipos de producto](/admin/tipos-producto), "
                    + "[Usuarios](/admin/usuarios), [Taller](/admin/taller/lista). "
                    + "El catálogo público es [Productos](/productos).";
            case RECEPCIONISTA -> "Perfil: recepcionista. Rutas: [Recepción](/admin/recepcion), "
                    + "[Taller — lista](/admin/taller/lista), [Seguimiento cliente](/reparaciones), "
                    + "[Productos tienda](/productos).";
            case TECNICO -> "Perfil: técnico de taller. Rutas: [Taller](/admin/taller/lista), "
                    + "[Reparaciones/gestión](/admin/taller/gestion) si aplica. "
                    + "[Productos](/productos) para la tienda.";
            case CLIENTE -> "Perfil: cliente. Rutas típicas: [Catálogo de productos](/productos), "
                    + "[Carrito](/carrito), [Tu panel / cuenta](/dashboard), "
                    + "[Seguimiento de reparaciones](/reparaciones). "
                    + "Para gestionar pedidos como cliente suele usarse el panel [dashboard](/dashboard).";
        };
    }

    private List<ChatMensaje> sliceHistory(List<ChatMensaje> full) {
        if (full.size() <= maxHistoryMessages) {
            return full;
        }
        return full.subList(full.size() - maxHistoryMessages, full.size());
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() <= MAX_CHARS_PER_MESSAGE) {
            return s;
        }
        return s.substring(0, MAX_CHARS_PER_MESSAGE);
    }
}
