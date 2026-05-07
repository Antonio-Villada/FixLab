package com.software.fixlab.service.impl;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PythonChatClient {

    @Value("${fixlab.chatbot.python.enabled:true}")
    private boolean enabled;

    @Value("${fixlab.chatbot.python.base-url:http://127.0.0.1:8090}")
    private String baseUrl;

    @Value("${fixlab.chatbot.python.max-history-messages:18}")
    private int maxHistoryMessages;

    private RestClient restClient;

    @PostConstruct
    void init() {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        log.info("PythonChatClient activo; enabled={}, baseUrl={}", enabled, baseUrl);
    }

    public Optional<String> generateReply(
            List<ChatMensaje> historyOrderedAsc,
            RolUsuario rolUsuario,
            String contextoTurno,
            String userText) {
        if (!enabled) {
            return Optional.empty();
        }
        if (userText == null || userText.isBlank()) {
            return Optional.empty();
        }

        try {
            List<ChatMensaje> slice = sliceHistory(historyOrderedAsc);
            List<Map<String, Object>> history = new ArrayList<>();
            for (ChatMensaje m : slice) {
                String role = m.getRol() == ChatRol.USER ? "USER" : "BOT";
                String text = m.getTexto() != null ? m.getTexto().trim() : "";
                if (!text.isBlank()) {
                    history.add(Map.of("role", role, "text", text));
                }
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("userText", userText.trim());
            body.put("usuarioRol", rolUsuario != null ? rolUsuario.name() : null);
            body.put("contextoTurno", contextoTurno != null && !contextoTurno.isBlank() ? contextoTurno.trim() : null);
            body.put("history", history);

            String raw = restClient.post()
                    .uri("/reply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (raw == null || raw.isBlank()) {
                return Optional.empty();
            }

            // Evitamos meter ObjectMapper aquí (para mantenerlo liviano); parseo simple.
            // Espera formato: {"text":"...","source":"IA"}
            String text = extractJsonString(raw, "text");
            if (text == null || text.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(text.trim());
        } catch (RestClientException e) {
            log.debug("PythonChatClient no disponible: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.debug("PythonChatClient error inesperado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private List<ChatMensaje> sliceHistory(List<ChatMensaje> full) {
        if (full == null || full.isEmpty()) {
            return List.of();
        }
        if (full.size() <= maxHistoryMessages) {
            return full;
        }
        return full.subList(full.size() - maxHistoryMessages, full.size());
    }

    private static String extractJsonString(String raw, String key) {
        if (raw == null) {
            return null;
        }
        String r = raw.trim();
        String k = "\"" + key + "\"";
        int i = r.indexOf(k);
        if (i < 0) {
            return null;
        }
        int colon = r.indexOf(':', i + k.length());
        if (colon < 0) {
            return null;
        }
        int firstQuote = r.indexOf('"', colon + 1);
        if (firstQuote < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int p = firstQuote + 1; p < r.length(); p++) {
            char c = r.charAt(p);
            if (escape) {
                // Manejo mínimo de escapes comunes.
                sb.append(switch (c) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> c;
                });
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}

