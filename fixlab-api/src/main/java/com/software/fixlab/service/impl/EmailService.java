package com.software.fixlab.service.impl;

import com.software.fixlab.util.EmailTemplateUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /** Si true, no envía correo real; solo imprime en consola (útil para desarrollo sin SMTP). */
    @Value("${fixlab.mail.log-only:false}")
    private boolean logOnly;

    @Value("${fixlab.mail.from:labfix64@gmail.com}")
    private String mailFrom;

    @Value("${fixlab.frontend.url:http://localhost:4200}")
    private String frontendBaseUrl;

    private String getLogoUrl() {
        String base = (frontendBaseUrl != null) ? frontendBaseUrl.trim() : "";
        if (base.isEmpty()) return null;
        return base.endsWith("/") ? base + "images/Logo.jpeg" : base + "/images/Logo.jpeg";
    }

    public void enviarCodigoVerificacion(String destino, String nombre, String codigo) {
        String html = EmailTemplateUtil.construirHtmlCodigo(
                nombre, codigo,
                "¡Bienvenido!",
                "Completa tu registro en FixLab",
                "Gracias por registrarte. Tu código de verificación es:",
                getLogoUrl()
        );
        enviarCorreoHtml(destino, "Tu código de verificación - FixLab", html);
    }

    /** Código de 6 dígitos para restablecer contraseña. */
    public void enviarCodigoRecuperacionPassword(String destino, String nombre, String codigo) {
        String html = EmailTemplateUtil.construirHtmlCodigo(
                nombre, codigo,
                "¡Listo!",
                "Restablece tu contraseña",
                "Has solicitado restablecer tu contraseña en FixLab. Tu código es:",
                getLogoUrl()
        );
        enviarCorreoHtml(destino, "Código para restablecer contraseña - FixLab", html);
    }

    /** Código de 6 dígitos para completar el inicio de sesión (2FA por correo). */
    public void enviarCodigoLogin2fa(String destino, String nombre, String codigo) {
        String html = EmailTemplateUtil.construirHtmlCodigo(
                nombre, codigo,
                "¡Listo!",
                "Inicia sesión en FixLab",
                "Tu código para iniciar sesión es:",
                getLogoUrl()
        );
        enviarCorreoHtml(destino, "Código de acceso - FixLab", html);
    }

    /**
     * Notifica al cliente dueño del equipo que el ticket de taller cambió de estado.
     * Si falla el envío, se puede relanzar según la política del llamador; aquí no se lanza desde métodos internos del helper HTML.
     */
    public void enviarNotificacionCambioEstadoReparacion(
            String destino,
            String nombreCliente,
            String numeroTicket,
            String estadoAnteriorLegible,
            String estadoNuevoLegible,
            String lineaExtra) {
        String nombre = nombreCliente != null && !nombreCliente.isBlank() ? nombreCliente.trim() : "cliente";
        String seguimientoUrl = construirUrlSeguimientoReparaciones();
        String logo = getLogoUrl();
        String logoBlock = (logo != null && !logo.isBlank())
                ? "<p style=\"margin:0 0 16px 0;\"><img src=\"" + logo + "\" alt=\"FixLab\" width=\"40\" height=\"40\" style=\"display:block;border:0;\" /></p>"
                : "";
        String extraBlock = (lineaExtra != null && !lineaExtra.isBlank())
                ? "<p style=\"color:#555;font-size:14px;margin:12px 0;\">" + escaparHtmlSimple(lineaExtra) + "</p>"
                : "";
        String html = "<!DOCTYPE html><html><body style=\"font-family:Segoe UI,Arial,sans-serif;background:#f6f7f9;margin:0;padding:24px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\">"
                + "<div style=\"max-width:520px;background:#fff;border-radius:8px;padding:28px;text-align:left;border:1px solid #e5e7eb;\">"
                + logoBlock
                + "<h1 style=\"font-size:18px;color:#111827;margin:0 0 8px;\">Actualización de tu orden de servicio</h1>"
                + "<p style=\"color:#374151;font-size:15px;margin:0 0 16px;\">Hola <strong>" + escaparHtmlSimple(nombre) + "</strong>,</p>"
                + "<p style=\"color:#374151;font-size:15px;margin:0 0 12px;\">El estado del ticket <strong style=\"font-family:monospace;\">"
                + escaparHtmlSimple(numeroTicket) + "</strong> ha cambiado:</p>"
                + "<table style=\"width:100%;border-collapse:collapse;margin:16px 0;font-size:14px;\">"
                + "<tr><td style=\"padding:10px 12px;background:#f3f4f6;color:#6b7280;\">Estado anterior</td>"
                + "<td style=\"padding:10px 12px;background:#f9fafb;\">" + escaparHtmlSimple(estadoAnteriorLegible) + "</td></tr>"
                + "<tr><td style=\"padding:10px 12px;background:#ecfdf5;color:#065f46;font-weight:600;\">Estado actual</td>"
                + "<td style=\"padding:10px 12px;background:#ecfdf5;font-weight:600;\">" + escaparHtmlSimple(estadoNuevoLegible) + "</td></tr>"
                + "</table>"
                + extraBlock
                + "<p style=\"margin:20px 0 0;\"><a href=\"" + escaparAttr(seguimientoUrl) + "\" style=\"display:inline-block;background:#198754;color:#fff;text-decoration:none;padding:10px 18px;border-radius:6px;font-size:14px;\">Ver seguimiento</a></p>"
                + "<p style=\"color:#9ca3af;font-size:12px;margin:24px 0 0;\">FixLab — Servicio técnico</p>"
                + "</div></td></tr></table></body></html>";

        enviarCorreoHtml(destino,
                "Actualización de tu orden " + numeroTicket + " - FixLab",
                html);
    }

    /**
     * Notifica al cliente cambios en su PQRS / garantía. Incluye aviso breve de tratamiento de datos (Ley 1581/2012).
     */
    public void enviarNotificacionCambioEstadoPqr(
            String destino,
            String nombreCliente,
            String radicado,
            String estadoAnteriorLegible,
            String estadoNuevoLegible,
            String tipoLegible,
            String lineaExtra) {
        String nombre = nombreCliente != null && !nombreCliente.isBlank() ? nombreCliente.trim() : "cliente";
        String seguimientoUrl = construirUrlMisPqrs();
        String logo = getLogoUrl();
        String logoBlock = (logo != null && !logo.isBlank())
                ? "<p style=\"margin:0 0 16px 0;\"><img src=\"" + logo + "\" alt=\"FixLab\" width=\"40\" height=\"40\" style=\"display:block;border:0;\" /></p>"
                : "";
        String extraBlock = (lineaExtra != null && !lineaExtra.isBlank())
                ? "<p style=\"color:#555;font-size:14px;margin:12px 0;\">" + escaparHtmlSimple(lineaExtra) + "</p>"
                : "";
        String html = "<!DOCTYPE html><html><body style=\"font-family:Segoe UI,Arial,sans-serif;background:#f6f7f9;margin:0;padding:24px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\">"
                + "<div style=\"max-width:520px;background:#fff;border-radius:8px;padding:28px;text-align:left;border:1px solid #e5e7eb;\">"
                + logoBlock
                + "<h1 style=\"font-size:18px;color:#111827;margin:0 0 8px;\">Actualización de tu solicitud (PQRS)</h1>"
                + "<p style=\"color:#374151;font-size:15px;margin:0 0 16px;\">Hola <strong>" + escaparHtmlSimple(nombre) + "</strong>,</p>"
                + "<p style=\"color:#374151;font-size:15px;margin:0 0 8px;\">Tipo: <strong>" + escaparHtmlSimple(tipoLegible) + "</strong></p>"
                + "<p style=\"color:#374151;font-size:15px;margin:0 0 12px;\">Radicado <strong style=\"font-family:monospace;\">"
                + escaparHtmlSimple(radicado) + "</strong></p>"
                + "<table style=\"width:100%;border-collapse:collapse;margin:16px 0;font-size:14px;\">"
                + "<tr><td style=\"padding:10px 12px;background:#f3f4f6;color:#6b7280;\">Estado anterior</td>"
                + "<td style=\"padding:10px 12px;background:#f9fafb;\">" + escaparHtmlSimple(estadoAnteriorLegible) + "</td></tr>"
                + "<tr><td style=\"padding:10px 12px;background:#ecfdf5;color:#065f46;font-weight:600;\">Estado actual</td>"
                + "<td style=\"padding:10px 12px;background:#ecfdf5;font-weight:600;\">" + escaparHtmlSimple(estadoNuevoLegible) + "</td></tr>"
                + "</table>"
                + extraBlock
                + "<p style=\"margin:20px 0 0;\"><a href=\"" + escaparAttr(seguimientoUrl) + "\" style=\"display:inline-block;background:#0d6efd;color:#fff;text-decoration:none;padding:10px 18px;border-radius:6px;font-size:14px;\">Ver mis solicitudes</a></p>"
                + "<p style=\"color:#9ca3af;font-size:11px;margin:24px 0 0;line-height:1.4;\">Tratamos tus datos conforme a nuestra política de privacidad y la normativa colombiana (Ley 1581 de 2012). "
                + "Puedes ejercer tus derechos ARCO contactándonos por los canales oficiales de FixLab.</p>"
                + "<p style=\"color:#9ca3af;font-size:12px;margin:12px 0 0;\">FixLab — Postventa</p>"
                + "</div></td></tr></table></body></html>";

        enviarCorreoHtml(destino, "Actualización PQRS " + radicado + " - FixLab", html);
    }

    private String construirUrlMisPqrs() {
        String base = (frontendBaseUrl != null) ? frontendBaseUrl.trim() : "";
        if (base.isEmpty()) return "/mis-pqrs";
        return base.endsWith("/") ? base + "mis-pqrs" : base + "/mis-pqrs";
    }

    private String construirUrlSeguimientoReparaciones() {
        String base = (frontendBaseUrl != null) ? frontendBaseUrl.trim() : "";
        if (base.isEmpty()) return "/reparaciones";
        return base.endsWith("/") ? base + "reparaciones" : base + "/reparaciones";
    }

    private static String escaparHtmlSimple(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escaparAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;");
    }

    /** Vista mínima para consola cuando log-only (no se envía correo). */
    private static String vistaPlanaParaLog(String html) {
        if (html == null || html.isBlank()) return "";
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    public void enviarFacturaVenta(String destino, String nombre, String numeroPedido, Double total) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destino);
        mensaje.setSubject("Factura de Venta - Pedido #" + numeroPedido + " - FixLab");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "¡Tu pago ha sido procesado exitosamente!\n\n"
                + "Adjuntamos los detalles de tu factura de venta:\n"
                + "--------------------------------------------------\n"
                + "No. de Pedido: " + numeroPedido + "\n"
                + "Estado: PAGADO\n"
                + "Total Pagado: $" + total + "\n"
                + "--------------------------------------------------\n\n"
                + "Ya estamos preparando tus productos para el envío o entrega.\n"
                + "¡Gracias por confiar en FixLab!");

        mailSender.send(mensaje);
    }

    /**
     * Envía un correo solo en HTML (text/html).
     * Respeta fixlab.mail.log-only.
     */
    private void enviarCorreoHtml(String destinatario, String asunto, String htmlBody) {
        if (logOnly) {
            String preview = vistaPlanaParaLog(htmlBody);
            log.warn("=== MODO DESARROLLO: correo NO enviado (fixlab.mail.log-only=true) ===");
            log.info("Para: {}", destinatario);
            log.info("Asunto: {}", asunto);
            log.info("Vista previa (solo log): {}", preview);
            log.warn("=== Modo desarrollo: revisa consola / vista previa ===");
            System.out.println("\n========== FIXLAB - CORREO HTML (modo desarrollo, no enviado) ==========");
            System.out.println("Para: " + destinatario);
            System.out.println("Asunto: " + asunto);
            System.out.println("--- Vista sin etiquetas (solo para leer en consola) ---");
            System.out.println(preview);
            System.out.println("========================================================================\n");
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom("FixLab Soporte <" + mailFrom + ">");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            log.info("Correo HTML enviado correctamente a {} (asunto: {})", destinatario, asunto);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Error al enviar correo", e);
        }
    }

    /** Envía un correo genérico en texto plano. Si fixlab.mail.log-only=true, solo imprime en consola. */
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        if (logOnly) {
            log.warn("=== MODO DESARROLLO: correo NO enviado (fixlab.mail.log-only=true) ===");
            log.info("Para: {}", destinatario);
            log.info("Asunto: {}", asunto);
            log.info("Cuerpo:\n{}", mensaje);
            log.warn("=== Modo desarrollo: copia el código o enlace de arriba ===");
            System.out.println("\n========== FIXLAB - CORREO (modo desarrollo, fixlab.mail.log-only=true) ==========");
            System.out.println("Para: " + destinatario);
            System.out.println("Asunto: " + asunto);
            System.out.println("--- Cuerpo ---");
            System.out.println(mensaje);
            System.out.println("===============================================================================\n");
            return;
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(destinatario);
        mailMessage.setSubject(asunto);
        mailMessage.setText(mensaje);
        mailMessage.setFrom("FixLab Soporte <" + mailFrom + ">");
        try {
            mailSender.send(mailMessage);
            log.info("Correo enviado correctamente a {} (asunto: {})", destinatario, asunto);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw e;
        }
    }
}