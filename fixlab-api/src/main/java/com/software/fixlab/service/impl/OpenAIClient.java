package com.software.fixlab.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Cliente para OpenAI Chat Completions. Si openai.api-key no está configurado, no realiza llamadas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Envía el mensaje del usuario a OpenAI con un system prompt de FixLab. Retorna vacío si no hay API key o hay error.
     */
    public Optional<String> completar(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OpenAI API key no configurado; se omite llamada a IA.");
            return Optional.empty();
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode system = objectMapper.createObjectNode();
            system.put("role", "system");
            system.put("content", systemPrompt);
            messages.add(system);
            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", userMessage);
            messages.add(user);
            body.set("messages", messages);
            body.put("max_tokens", 500);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("OpenAI respondió con status {} o body vacío", response.getStatusCode());
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                return Optional.empty();
            }
            String content = choices.get(0).path("message").path("content").asText("");
            return Optional.ofNullable(content.isBlank() ? null : content.trim());
        } catch (Exception e) {
            log.warn("Error al llamar a OpenAI: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
