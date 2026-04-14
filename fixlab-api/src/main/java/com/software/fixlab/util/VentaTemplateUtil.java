package com.software.fixlab.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Plantillas HTML para Proceso 2 (ventas): email de confirmación y factura (PDF).
 * Se renderiza con placeholders simples para evitar dependencias de motor de templates.
 */
public final class VentaTemplateUtil {

    private static final String EMAIL_TEMPLATE_PATH = "templates/emails/confirmacion-compra.html";
    private static final String PDF_TEMPLATE_PATH = "templates/pdf/factura.html";

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private VentaTemplateUtil() {
    }

    public record VentaItemRow(String nombreProducto, int cantidad, double precioUnitario) {
        public double subtotal() {
            return cantidad * precioUnitario;
        }
    }

    public static String renderEmailConfirmacionCompra(
            String nombreCliente,
            String logoUrl,
            String pedidoId,
            List<VentaItemRow> items,
            double total,
            String buttonUrl
    ) {
        String html = cargarTemplate(EMAIL_TEMPLATE_PATH);
        if (html == null) {
            // fallback mínimo si falta el archivo
            return "<!doctype html><html><body>"
                    + "<p>Hola <strong>" + esc(nombreCliente) + "</strong>,</p>"
                    + "<p>Tu pago fue exitoso. Pedido #" + esc(pedidoId) + " por " + esc(fmtMoney(total)) + ".</p>"
                    + "<p>Ver estado: <a href=\"" + escAttr(buttonUrl) + "\">" + esc(buttonUrl) + "</a></p>"
                    + "</body></html>";
        }

        String logoImg = (logoUrl != null && !logoUrl.isBlank())
                ? "<img src=\"" + escAttr(logoUrl) + "\" alt=\"FixLab\" width=\"36\" height=\"36\" style=\"display:block;max-width:36px;height:auto;border:0;\" />"
                : "";

        return html
                .replace("{{LOGO_IMG}}", logoImg)
                .replace("{{NOMBRE}}", esc(nombreCliente))
                .replace("{{PEDIDO_ID}}", esc(pedidoId))
                .replace("{{ITEMS_ROWS}}", buildEmailItemsRows(items))
                .replace("{{TOTAL}}", esc(fmtMoney(total)))
                .replace("{{BUTTON_URL}}", escAttr(buttonUrl));
    }

    public static String renderFacturaHtml(
            String nombreCliente,
            String logoUrl,
            String emailCliente,
            String direccionEnvio,
            String pedidoId,
            LocalDateTime fecha,
            List<VentaItemRow> items,
            double total
    ) {
        String html = cargarTemplate(PDF_TEMPLATE_PATH);
        if (html == null) {
            return "<!doctype html><html><body>"
                    + "<h3>FixLab - Factura</h3>"
                    + "<p>Pedido #" + esc(pedidoId) + " - " + esc(fmtMoney(total)) + "</p>"
                    + "</body></html>";
        }

        String logoImg = (logoUrl != null && !logoUrl.isBlank())
                ? "<img src=\"" + escAttr(logoUrl) + "\" alt=\"FixLab\" width=\"36\" height=\"36\" style=\"display:block;max-width:36px;height:auto;border:0;\" />"
                : "";

        return html
                .replace("{{LOGO_IMG}}", logoImg)
                .replace("{{NOMBRE}}", esc(nombreCliente))
                .replace("{{EMAIL}}", esc(emailCliente))
                .replace("{{DIRECCION}}", esc(direccionEnvio != null ? direccionEnvio : ""))
                .replace("{{PEDIDO_ID}}", esc(pedidoId))
                .replace("{{FECHA}}", esc(FECHA_FMT.format(fecha != null ? fecha : LocalDateTime.now())))
                .replace("{{ITEMS_ROWS}}", buildPdfItemsRows(items))
                .replace("{{TOTAL}}", esc(fmtMoney(total)));
    }

    private static String buildEmailItemsRows(List<VentaItemRow> items) {
        StringBuilder sb = new StringBuilder();
        for (VentaItemRow it : items != null ? items : List.<VentaItemRow>of()) {
            sb.append("<tr>")
                    .append("<td style=\"padding:12px 12px;border-top:1px solid #e5e7eb;color:#111827;font-size:13px;\">")
                    .append(esc(it.nombreProducto()))
                    .append("</td>")
                    .append("<td style=\"padding:12px 12px;border-top:1px solid #e5e7eb;color:#111827;font-size:13px;text-align:center;\">")
                    .append(it.cantidad())
                    .append("</td>")
                    .append("<td style=\"padding:12px 12px;border-top:1px solid #e5e7eb;color:#111827;font-size:13px;text-align:right;\">")
                    .append(esc(fmtMoney(it.subtotal())))
                    .append("</td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    private static String buildPdfItemsRows(List<VentaItemRow> items) {
        StringBuilder sb = new StringBuilder();
        for (VentaItemRow it : items != null ? items : List.<VentaItemRow>of()) {
            sb.append("<tr>")
                    .append("<td>").append(esc(it.nombreProducto())).append("</td>")
                    .append("<td class=\"num\">").append(it.cantidad()).append("</td>")
                    .append("<td class=\"num\">").append(esc(fmtMoney(it.precioUnitario()))).append("</td>")
                    .append("<td class=\"num\">").append(esc(fmtMoney(it.subtotal()))).append("</td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    private static String fmtMoney(double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_CO);
        return nf.format(value);
    }

    private static String cargarTemplate(String classpathLocation) {
        try (InputStream is = VentaTemplateUtil.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (is == null) return null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;");
    }
}

