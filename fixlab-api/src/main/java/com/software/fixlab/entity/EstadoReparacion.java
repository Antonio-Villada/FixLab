package com.software.fixlab.entity;

/**
 * Estados del flujo de servicio técnico (Proceso 3).
 */
public enum EstadoReparacion {
    RECIBIDO,
    EN_DIAGNOSTICO,
    COTIZADO_PENDIENTE_APROBACION,
    APROBADO,
    EN_REPARACION,
    EN_PRUEBAS,
    LISTO_ENTREGA,
    ENTREGADO,
    CANCELADO
}
