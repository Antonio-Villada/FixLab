package com.software.fixlab.entity;

/** Documento que respalda la solicitud: ventas (pedido), servicio (reparación) o sin vínculo. */
public enum OrigenDocumentoPqr {
    FACTURA_PEDIDO,
    TICKET_REPARACION,
    /** Petición, queja o sugerencia radicadas sin factura ni ticket (opcional según tipo). */
    SIN_REFERENCIA
}
