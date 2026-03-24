package com.software.fixlab.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para cargar plantillas HTML de correo y reemplazar placeholders.
 */
public final class EmailTemplateUtil {

    private static final String TEMPLATE_PATH = "templates/emails/codigo-verificacion.html";

    private EmailTemplateUtil() {
    }

    /**
     * Carga la plantilla de código de verificación y reemplaza los placeholders.
     *
     * @param nombre   Nombre del destinatario
     * @param codigo   Código de 6 dígitos
     * @param titulo   Título pequeño (ej: "¡Listo!", "¡Bienvenido!")
     * @param subtitulo Subtítulo principal (ej: "Completa tu registro en FixLab")
     * @param mensaje  Párrafo explicativo antes del código
     * @param logoUrl  URL absoluta del logo (ej: https://tuapp.com/images/Logo.jpeg). Si null o vacío, no se muestra.
     * @return HTML listo para enviar
     */
    public static String construirHtmlCodigo(String nombre, String codigo, String titulo, String subtitulo, String mensaje, String logoUrl) {
        String html = cargarTemplate();
        if (html == null) {
            return construirHtmlFallback(nombre, codigo, titulo, subtitulo, mensaje);
        }
        String logoImg = (logoUrl != null && !logoUrl.isBlank())
                ? "<img src=\"" + escaparHtml(logoUrl) + "\" alt=\"FixLab\" width=\"36\" height=\"36\" style=\"display:block; max-width:36px; height:auto; border:0;\" />"
                : "";
        return html
                .replace("{{NOMBRE}}", escaparHtml(nombre))
                .replace("{{CODIGO}}", codigo)
                .replace("{{TITULO}}", escaparHtml(titulo))
                .replace("{{SUBTITULO}}", escaparHtml(subtitulo))
                .replace("{{MENSAJE}}", escaparHtml(mensaje))
                .replace("{{ASUNTO}}", "FixLab")
                .replace("{{LOGO_IMG}}", logoImg);
    }

    private static String cargarTemplate() {
        try (InputStream is = EmailTemplateUtil.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) return null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static String escaparHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String construirHtmlFallback(String nombre, String codigo, String titulo, String subtitulo, String mensaje) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;'>"
                + "<p>Hola <strong>" + escaparHtml(nombre) + "</strong>,</p>"
                + "<h3>" + escaparHtml(titulo) + "</h3>"
                + "<p>" + escaparHtml(subtitulo) + "</p>"
                + "<p>" + escaparHtml(mensaje) + "</p>"
                + "<p style='font-size:24px; font-weight:bold;'>" + codigo + "</p>"
                + "<p>Este código expira en 15 minutos.</p>"
                + "</body></html>";
    }
}
