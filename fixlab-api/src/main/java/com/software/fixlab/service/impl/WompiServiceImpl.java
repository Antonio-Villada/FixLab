package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.WompiTransactionDTO;
import com.software.fixlab.dto.req.WompiWebhookDTO;
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
        String secret = integritySecret != null ? integritySecret.trim() : "";
        String cadena = referencia + montoEnCentavos + moneda + secret;
        return sha256Hex(cadena);
    }

    @Override
    public boolean validarFirmaEvento(WompiWebhookDTO evento) {
        try {
            if (evento == null || evento.getData() == null || evento.getData().getTransaction() == null
                    || evento.getSignature() == null || evento.getSignature().getChecksum() == null
                    || evento.getSignature().getProperties() == null || evento.getTimestamp() == null) {
                return false;
            }
            String[] properties = evento.getSignature().getProperties();
            WompiTransactionDTO tx = evento.getData().getTransaction();
            StringBuilder sb = new StringBuilder();
            for (String prop : properties != null ? properties : new String[0]) {
                String val = resolveProperty(prop, tx);
                if (val != null) sb.append(val);
            }
            String secret = eventsSecret != null ? eventsSecret.trim() : "";
            sb.append(evento.getTimestamp()).append(secret);
            String firmaCalculada = sha256Hex(sb.toString());
            return firmaCalculada.equalsIgnoreCase(evento.getSignature().getChecksum());
        } catch (Exception e) {
            return false;
        }
    }

    private static String resolveProperty(String property, WompiTransactionDTO tx) {
        if (tx == null) return null;
        switch (property) {
            case "transaction.id": return tx.getId();
            case "transaction.status": return tx.getStatus();
            case "transaction.amount_in_cents": return tx.getAmount_in_cents() != null ? String.valueOf(tx.getAmount_in_cents()) : null;
            case "transaction.reference": return tx.getReference();
            default: return null;
        }
    }

    private static String sha256Hex(String texto) throws Exception {
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
