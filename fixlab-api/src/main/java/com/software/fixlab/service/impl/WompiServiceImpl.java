package com.software.fixlab.service.impl;

import com.software.fixlab.service.interfaces.WompiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class WompiServiceImpl implements WompiService {

    @Value("${wompi.integrity-secret}")
    private String integritySecret;

    @Value("${wompi.events-secret}")
    private String eventsSecret;

    @Override
    public String generarFirma(String referencia, Long montoEnCentavos, String moneda) throws Exception {
        // Concatenación para crear pedido: Referencia + Monto + Moneda + Secreto de Integridad
        String cadena = referencia + montoEnCentavos + moneda + integritySecret;
        return encriptarSHA256(cadena);
    }

    @Override
    public boolean validarFirmaEvento(String transaccionId, String estado, Long montoEnCentavos, Long timestamp, String firmaWompi) {
        try {
            // Concatenación para Webhook: ID + Estado + Monto + Timestamp + Secreto de Eventos
            String cadena = transaccionId + estado + montoEnCentavos + timestamp + eventsSecret;
            String firmaCalculada = encriptarSHA256(cadena);

            return firmaCalculada.equalsIgnoreCase(firmaWompi);
        } catch (Exception e) {
            return false;
        }
    }

    // Método auxiliar para no repetir código de encriptación
    private String encriptarSHA256(String texto) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}