package com.software.fixlab.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.fixlab.entity.ChatMensaje;
import com.software.fixlab.entity.ChatRol;
import com.software.fixlab.entity.RolUsuario;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OllamaChatClient {

    /**
     * Límite defensivo por mensaje para no reventar RAM/ctx en PCs modestos (8GB).
     * Ollama/phi3 pueden caerse si el prompt crece demasiado.
     */
    private static final int MAX_CHARS_PER_MESSAGE = 1200;

    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.model:phi3}")
    private String model;

    @Value("${ollama.max-history-messages:18}")
    private int maxHistoryMessages;

    @Value("${ollama.num-ctx:2048}")
    private int numCtx;

    @Value("${ollama.num-predict:256}")
    private int numPredict;

    /**
     * Ngrok (plan free) puede insertar una pantalla de advertencia que devuelve HTML/403 a clientes "no navegador".
     * Este header es el bypass recomendado por la comunidad/ngrok para integraciones.
     */
    @Value("${ollama.ngrok-skip-browser-warning:true}")
    private boolean ngrokSkipBrowserWarning;

    private RestClient restClient;

    private static final String SYSTEM_PROMPT =
            "Eres el asistente virtual de FixLab (software para tienda + taller: catálogo, pedidos/pagos, taller/reparaciones y administración). "
                    + "Responde SIEMPRE en español. "
                    + "Estilo: 2 a 4 oraciones, máximo ~90 palabras, tono profesional. "
                    + "Debes cerrar la idea: termina en punto final (.) y NO cortes palabras a la mitad. "
                    + "No inventes funciones, integraciones ni políticas. Si no estás seguro, dilo en una frase y orienta con 1 ruta interna. "
                    + "Enlaces Markdown SOLO si ayudan (máx. 1) y SOLO con rutas que empiecen por / (ej. [/productos](/productos)). "
                    + "No uses rutas inventadas (evita cosas como ./categoria/). "
                    + "Evita listas largas. "
                    + "No escribas metatexto del sistema (no digas que tienes acceso, no menciones tu rol, no digas \"contexto\"). "
                    + "No repitas el encabezado \"## Contexto de esta petición\".";

    public OllamaChatClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);

        // Evita el interstitial HTML/403 de ngrok free en llamadas server-to-server.
        if (ngrokSkipBrowserWarning && looksLikeNgrokFreeHost(baseUrl)) {
            builder.defaultHeader("ngrok-skip-browser-warning", "true");
            // Algunos proxies son estrictos con User-Agent vacío; esto ayuda en integraciones.
            builder.defaultHeader(HttpHeaders.USER_AGENT, "FixLabBackend/1.0");
        }

        this.restClient = builder.build();
        log.info("Ollama activo; baseUrl={}, model={}", baseUrl, model);
    }

    /**
     * Genera la respuesta del modelo a partir del historial completo del usuario (orden ascendente por fecha).
     * Si Ollama no está disponible o falla la petición, devuelve vacío para usar un respaldo local.
     *
     * @param contextoTurno Texto libre (pantalla actual, carrito, resumen de cuenta) solo para esta respuesta;
     *                      no se persiste en el historial del chat.
     */
    public Optional<String> generateReply(
            List<ChatMensaje> historyOrderedAsc,
            RolUsuario rolUsuario,
            String contextoTurno) {
        if (historyOrderedAsc == null || historyOrderedAsc.isEmpty()) {
            return Optional.empty();
        }

        List<ChatMensaje> slice = sliceHistory(historyOrderedAsc);
        List<Map<String, Object>> messages = new ArrayList<>();

        String systemText = SYSTEM_PROMPT + "\n\n" + navigationHintForRole(rolUsuario);
        if (contextoTurno != null && !contextoTurno.isBlank()) {
            systemText += "\n\n## Contexto de esta petición (datos reales enviados por la app; úsalos en la respuesta)\n"
                    + contextoTurno.trim();
        }
        messages.add(Map.of("role", "system", "content", systemText));

        for (ChatMensaje m : slice) {
            String role = m.getRol() == ChatRol.USER ? "user" : "assistant";
            String text = truncate(m.getTexto());
            if (text.isBlank()) {
                continue;
            }
            messages.add(Map.of("role", role, "content", text));
        }

        if (messages.size() <= 1) {
            return Optional.empty();
        }

        try {
            Optional<String> first = doChat(messages, numCtx, numPredict);
            if (first.isPresent()) {
                return first;
            }

            // Reintento ultra-ligero si el runner se muere por memoria/contexto.
            List<Map<String, Object>> retryMessages = new ArrayList<>();
            retryMessages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            List<ChatMensaje> retrySlice = sliceHistoryUltraLight(historyOrderedAsc);
            for (ChatMensaje m : retrySlice) {
                String role = m.getRol() == ChatRol.USER ? "user" : "assistant";
                String text = truncate(m.getTexto());
                if (!text.isBlank()) {
                    retryMessages.add(Map.of("role", role, "content", text));
                }
            }
            // Reintento: menos contexto, pero con suficiente num_predict para no cortar a mitad de frase.
            return doChat(retryMessages, Math.min(768, numCtx), Math.min(200, numPredict));
        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            String snippet = errorBody != null && errorBody.length() > 800
                    ? errorBody.substring(0, 800) + "…"
                    : errorBody;
            log.warn(
                    "Ollama API rechazó la petición: status={} — {}. baseUrl={}, model={}",
                    e.getStatusCode().value(),
                    snippet != null ? snippet : e.getMessage(),
                    baseUrl,
                    model);
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Ollama error de red/cliente: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase(Locale.ROOT).contains("json")) {
                log.warn("Ollama: respuesta JSON inválida: {}", msg);
            } else {
                log.warn("Ollama: error inesperado: {}", msg);
            }
            return Optional.empty();
        }
    }

    private Optional<String> doChat(List<Map<String, Object>> messages, int ctx, int predict) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("options", Map.of(
                "num_ctx", ctx,
                "num_predict", predict
        ));

        String raw = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            String text = root.path("message").path("content").asText("").trim();
            text = postprocessAssistantText(text);
            return text.isBlank() ? Optional.empty() : Optional.of(text);
        } catch (Exception e) {
            String snippet = raw.length() > 300 ? raw.substring(0, 300) + "…" : raw;
            log.warn("Ollama: respuesta no-JSON (posible HTML/ngrok). Snippet={}", snippet);
            return Optional.empty();
        }
    }

    private List<ChatMensaje> sliceHistory(List<ChatMensaje> full) {
        if (full.size() <= maxHistoryMessages) {
            return full;
        }
        return full.subList(full.size() - maxHistoryMessages, full.size());
    }

    private List<ChatMensaje> sliceHistoryUltraLight(List<ChatMensaje> full) {
        int max = 6;
        if (full.size() <= max) {
            return full;
        }
        return full.subList(full.size() - max, full.size());
    }

    private static String navigationHintForRole(RolUsuario rol) {
        if (rol == null) {
            rol = RolUsuario.CLIENTE;
        }
        return switch (rol) {
            case ADMIN -> "Si el usuario pide navegar, sugiere como máximo 1 ruta real: "
                    + "/admin/productos, /admin/pedidos, /admin/taller/lista o /admin/usuarios.";
            case RECEPCIONISTA -> "Si pide navegar, sugiere como máximo 1 ruta real: "
                    + "/admin/recepcion, /admin/taller/lista o /reparaciones.";
            case TECNICO -> "Si pide navegar, sugiere como máximo 1 ruta real: "
                    + "/admin/taller/lista o /admin/taller/gestion.";
            case CLIENTE -> "Si pide navegar, sugiere como máximo 1 ruta real: "
                    + "/productos, /carrito, /dashboard o /reparaciones.";
        };
    }

    /**
     * Evita respuestas que “cortan” a mitad de palabra cuando el modelo se queda sin tokens.
     * No intenta reescribir contenido; solo recorta al último espacio si el final parece incompleto.
     */
    private static String postprocessAssistantText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String t = text.trim();
        if (t.length() < 40) {
            return t;
        }
        char last = t.charAt(t.length() - 1);
        boolean endsClean = last == '.' || last == '!' || last == '?' || last == '…' || last == '"' || last == ')';
        if (endsClean) {
            return t;
        }
        int idx = t.lastIndexOf(' ');
        if (idx <= 0 || idx >= t.length() - 1) {
            return t;
        }
        // Si el último “token” parece una palabra truncada (muy larga sin puntuación), recorta al último espacio.
        String tail = t.substring(idx + 1);
        if (tail.length() >= 18 && !tail.contains("/") && !tail.contains("]")) {
            return t.substring(0, idx).trim() + ".";
        }
        return t;
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

    private static boolean looksLikeNgrokFreeHost(String url) {
        if (url == null) {
            return false;
        }
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("ngrok-free.app") || u.contains("ngrok-free.dev");
    }
}

