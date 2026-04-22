package com.software.fixlab.util;

import java.util.regex.Pattern;

/**
 * Quita de la respuesta visible del bot fragmentos que a veces repite el modelo
 * (eco del bloque de contexto enviado en systemInstruction).
 */
public final class ChatReplySanitizer {

    private static final Pattern LEADING_OR_EMBEDDED_CONTEXT_BLOCK = Pattern.compile(
            "(?is)(?:^|\\R)\\s*#{1,3}\\s*Contexto de esta petición\\b.*?((?=\\R\\s*\\R)|\\z)");

    /** Líneas tipo "Contexto: ..." (metatexto que no debe ver el usuario). */
    private static final Pattern CONTEXTO_COLON_LINE = Pattern.compile(
            "(?im)^\\s*Contexto\\s*:.*$");

    /** Metatexto frecuente del modelo: "Tienes acceso..." / "como cliente/admin...". */
    private static final Pattern ACCESO_FIXLAB_LINE = Pattern.compile(
            "(?im)^\\s*Tienes acceso al software FixLab\\b.*$");

    private ChatReplySanitizer() {}

    public static String stripLeakedContextEcho(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String cleaned = LEADING_OR_EMBEDDED_CONTEXT_BLOCK.matcher(text).replaceAll("\n");
        cleaned = CONTEXTO_COLON_LINE.matcher(cleaned).replaceAll("");
        cleaned = ACCESO_FIXLAB_LINE.matcher(cleaned).replaceAll("");
        cleaned = cleaned.replaceAll("(?s)^\\s*\n+", "").trim();
        cleaned = cleaned.replaceAll("(?s)\n{3,}", "\n\n").trim();
        return cleaned.isBlank() ? text.trim() : cleaned;
    }
}
