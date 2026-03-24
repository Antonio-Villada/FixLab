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
        enviarCorreoHtml(destino, "Tu código de verificación - FixLab", html,
                "Hola " + nombre + ", Gracias por registrarte en FixLab. Tu código de verificación es: " + codigo + ". Este código expirará en 15 minutos.");
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
        enviarCorreoHtml(destino, "Código para restablecer contraseña - FixLab", html,
                "Hola " + nombre + ", Has solicitado restablecer tu contraseña. Tu código es: " + codigo + ". Este código expira en 15 minutos.");
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
        enviarCorreoHtml(destino, "Código de acceso - FixLab", html,
                "Hola " + nombre + ", Tu código para iniciar sesión es: " + codigo + ". Este código expira en 15 minutos.");
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
     * Envía un correo HTML con fallback a texto plano.
     * Respeta fixlab.mail.log-only.
     */
    private void enviarCorreoHtml(String destinatario, String asunto, String htmlBody, String textoPlano) {
        if (logOnly) {
            log.warn("=== MODO DESARROLLO: correo NO enviado (fixlab.mail.log-only=true) ===");
            log.info("Para: {}", destinatario);
            log.info("Asunto: {}", asunto);
            log.info("Cuerpo (texto): {}", textoPlano);
            log.warn("=== Modo desarrollo: copia el código de arriba ===");
            System.out.println("\n========== FIXLAB - CORREO (modo desarrollo) ==========");
            System.out.println("Para: " + destinatario);
            System.out.println("Asunto: " + asunto);
            System.out.println("--- Código en el correo ---");
            System.out.println(textoPlano);
            System.out.println("========================================================\n");
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("FixLab Soporte <" + mailFrom + ">");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(textoPlano, htmlBody);
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