package com.software.fixlab.service.impl;

import com.software.fixlab.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    // Este es el Bean que Spring buscará en el pom.xml (spring-boot-starter-mail)
    private final JavaMailSender mailSender;

    @Override
    public void enviarCodigoVerificacion(String email, String nombre, String codigo) {
        String mensaje = "<h1>Bienvenido a FixLab, " + nombre + "</h1>"
                + "<p>Tu código de seguridad para verificar la cuenta es: <b>" + codigo + "</b></p>"
                + "<p>Si no solicitaste este código, ignora este mensaje.</p>";

        enviarEmail(email, "Verifica tu cuenta - FixLab", mensaje);
    }

    @Override
    public void enviarFacturaVenta(String email, String nombre, String pedidoId, Double total) {
        String mensaje = "<h2>¡Gracias por elegir FixLab, " + nombre + "!</h2>"
                + "<p>Confirmamos el pago de tu pedido <b>#" + pedidoId + "</b>.</p>"
                + "<p>Monto total: <b>$" + total + " COP</b></p>"
                + "<p>Estamos preparando tus repuestos para el envío.</p>";

        enviarEmail(email, "Factura de Compra - Pedido #" + pedidoId, mensaje);
    }

    private void enviarEmail(String destinatario, String asunto, String contenido) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Logueamos en consola para que veas si falla el SMTP
            System.err.println("❌ Error crítico enviando correo a " + destinatario + ": " + e.getMessage());
        }
    }
}