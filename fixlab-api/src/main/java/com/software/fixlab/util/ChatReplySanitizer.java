package com.software.fixlab.util;

import java.util.regex.Pattern;

/**
 * Quita de la respuesta visible del bot fragmentos que a veces repite el modelo
 * (eco del bloque de contexto enviado en systemInstruction).
 */
public final class ChatReplySanitizer {

    private static final Pattern LEADING_OR_EMBEDDED_CONTEXT_BLOCK = Pattern.compile(
            "(?is)(?:^|\\R)\\s*#{1,3}\\s*Contexto de esta petición\\b.*?((?=\\R\\s*\\R)|\\z)");

    private ChatReplySanitizer() {}

    public static String stripLeakedContextEcho(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String cleaned = LEADING_OR_EMBEDDED_CONTEXT_BLOCK.matcher(text).replaceAll("\n");
        cleaned = cleaned.replaceAll("(?s)^\\s*\n+", "").trim();
        return cleaned.isBlank() ? text.trim() : cleaned;
    }
}
