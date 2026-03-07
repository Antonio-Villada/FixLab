package com.software.fixlab.service.impl;

import com.software.fixlab.service.interfaces.WompiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
public class WompiServiceImpl implements WompiService {

    @Value("${wompi.integrity-secret}")
    private String integritySecret;

    @Value("${wompi.events-secret}")
    private String eventsSecret;

    @Override
    public String generarFirma(String referencia, Long montoEnCentavos, String moneda) {
        try {
            // Concatenación exacta exigida por Wompi: Referencia + Monto + Moneda + Secreto de Integridad
            String cadena = referencia + montoEnCentavos + moneda + integritySecret;
            String firma = encriptarSHA256(cadena);

            log.info("Firma de integridad generada para la referencia: {}", referencia);
            return firma;
        } catch (Exception e) {
            log.error("Error fatal generando firma Wompi para referencia: {}", referencia, e);
            throw new RuntimeException("Error interno al procesar la firma de pago.");
        }
    }

    @Override
    public boolean validarFirmaEvento(String transaccionId, String estado, Long montoEnCentavos, Long timestamp, String firmaWompi) {
        try {
            // Concatenación exacta para Webhook: ID + Estado + Monto + Timestamp + Secreto de Eventos
            String cadena = transaccionId + estado + montoEnCentavos + timestamp + eventsSecret;
            String firmaCalculada = encriptarSHA256(cadena);

            boolean esValida = firmaCalculada.equalsIgnoreCase(firmaWompi);

            if (esValida) {
                log.info("Webhook Wompi validado con éxito para la transacción: {}", transaccionId);
            } else {
                log.warn("Alerta de Seguridad: Firma Wompi inválida. Transacción: {}. Esperada: {}, Recibida: {}",
                        transaccionId, firmaCalculada, firmaWompi);
            }

            return esValida;
        } catch (Exception e) {
            log.error("Error validando firma de evento Wompi para transacción: {}", transaccionId, e);
            return false; // Ante cualquier error de cálculo, rechazamos la petición por seguridad
        }
    }

    /**
     * Método auxiliar para centralizar la lógica criptográfica SHA-256
     */
        private String encriptarSHA256(String texto) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}