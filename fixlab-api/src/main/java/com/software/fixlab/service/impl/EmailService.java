package com.software.fixlab.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /** Si true, no envía correo real; solo imprime en consola (útil para desarrollo sin SMTP). */
    @Value("${fixlab.mail.log-only:false}")
    private boolean logOnly;

    public void enviarCodigoVerificacion(String destino, String nombre, String codigo) {
        if (logOnly) {
            logCodigoEnConsola("REGISTRO - Código verificación", destino, nombre, codigo, 15);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("FixLab Soporte <tu_correo@gmail.com>");
        mensaje.setTo(destino);
        mensaje.setSubject("Tu código de verificación - FixLab");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Gracias por registrarte en FixLab. Tu código de verificación es:\n\n"
                + "👉 " + codigo + " 👈\n\n"
                + "Este código expirará en 15 minutos.\n"
                + "Si no solicitaste este registro, ignora este mensaje.");

        mailSender.send(mensaje);
    }

    /** Código de 6 dígitos para completar el inicio de sesión (2FA). */
    public void enviarCodigoLogin(String destino, String nombre, String codigo) {
        if (logOnly) {
            logCodigoEnConsola("LOGIN - Código 2FA", destino, nombre, codigo, 5);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("FixLab Soporte <tu_correo@gmail.com>");
        mensaje.setTo(destino);
        mensaje.setSubject("Tu código para iniciar sesión - FixLab");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Has solicitado iniciar sesión en FixLab. Tu código de verificación es:\n\n"
                + "👉 " + codigo + " 👈\n\n"
                + "Este código expirará en 5 minutos.\n"
                + "Si no fuiste tú, ignora este mensaje y cambia tu contraseña.");

        mailSender.send(mensaje);
    }

    private void logCodigoEnConsola(String tipo, String destino, String nombre, String codigo, int minutos) {
        log.warn("=== MODO LOCAL: correo NO enviado (fixlab.mail.log-only=true) ===");
        System.out.println("\n========== FIXLAB - " + tipo + " (modo desarrollo) ==========");
        System.out.println("Para: " + destino + " (" + nombre + ")");
        System.out.println("CÓDIGO: " + codigo + "  (válido " + minutos + " min)");
        System.out.println("================================================================\n");
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

    /** Envía un correo genérico (p. ej. recuperación de contraseña). Si fixlab.mail.log-only=true, solo imprime en consola. */
    public void enviarCorreo(String destinatario, String asunto, String mensaje) {
        if (logOnly) {
            log.warn("=== MODO DESARROLLO: correo NO enviado (fixlab.mail.log-only=true) ===");
            log.info("Para: {}", destinatario);
            log.info("Asunto: {}", asunto);
            log.info("Cuerpo:\n{}", mensaje);
            log.warn("=== Copia el enlace de arriba para probar restablecer contraseña ===");
            // Imprimir también a consola para que siempre sea visible (no depende del nivel de log)
            System.out.println("\n========== FIXLAB - ENLACE RESTABLECER CONTRASEÑA (modo desarrollo) ==========");
            System.out.println("Para: " + destinatario);
            System.out.println("Asunto: " + asunto);
            System.out.println("--- Cuerpo (copia el enlace y ábrelo en el navegador) ---");
            System.out.println(mensaje);
            System.out.println("================================================================================\n");
            return;
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(destinatario);
        mailMessage.setSubject(asunto);
        mailMessage.setText(mensaje);
        mailMessage.setFrom("labfix64@gmail.com");
        try {
            mailSender.send(mailMessage);
            log.info("Correo enviado correctamente a {} (asunto: {})", destinatario, asunto);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw e;
        }
    }
}