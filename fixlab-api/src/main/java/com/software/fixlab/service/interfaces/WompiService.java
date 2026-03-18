package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.WompiWebhookDTO;

public interface WompiService {
    String generarFirma(String referencia, Long montoEnCentavos, String moneda) throws Exception;
    /** Valida el checksum del evento usando signature.properties + timestamp + secreto (docs Wompi Colombia). */
    boolean validarFirmaEvento(WompiWebhookDTO evento);
}