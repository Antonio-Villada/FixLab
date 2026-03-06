package com.software.fixlab.service.interfaces;

public interface WompiService {
    String generarFirma(String referencia, Long montoEnCentavos, String moneda) throws Exception;
    boolean validarFirmaEvento(String transaccionId, String estado, Long montoEnCentavos, Long timestamp, String firmaWompi);
}