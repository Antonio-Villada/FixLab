package com.software.fixlab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCodigoVerificacion(String destino, String nombre, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("FixLab Soporte <tu_correo@gmail.com>"); // Opcional, Spring usa el de properties por defecto
        mensaje.setTo(destino);
        mensaje.setSubject("Tu código de verificación - FixLab");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Gracias por registrarte en FixLab. Tu código de verificación es:\n\n"
                + "👉 " + codigo + " 👈\n\n"
                + "Este código expirará en 15 minutos.\n"
                + "Si no solicitaste este registro, ignora este mensaje.");

        mailSender.send(mensaje);
    }
}