package com.software.fixlab.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class WompiServiceImpl {

    @Value("${wompi.integrity-secret}")
    private String integritySecret;

    public String generarFirma(String referencia, Long montoEnCentavos, String moneda) {
        // La fórmula de Wompi: referencia + montoEnCentavos + moneda + secreto_integridad
        String cadenaOriginal = referencia + montoEnCentavos + moneda + integritySecret;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadenaOriginal.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // Convierte a cadena Hexadecimal
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando firma de seguridad", e);
        }
    }

    @Value("${wompi.events-secret}")
    private String eventsSecret;

    public boolean validarFirmaEvento(String idEvento, String status, Long monto, Long timestamp, String firmaRecibida) {
        // Fórmula de Wompi para eventos: id + status + amount_in_cents + timestamp + events_secret
        String cadenaOriginal = idEvento + status + monto + timestamp + eventsSecret;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cadenaOriginal.getBytes(StandardCharsets.UTF_8));
            String firmaCalculada = HexFormat.of().formatHex(hash);

            return firmaCalculada.equals(firmaRecibida);
        } catch (Exception e) {
            return false;
        }
    }
}