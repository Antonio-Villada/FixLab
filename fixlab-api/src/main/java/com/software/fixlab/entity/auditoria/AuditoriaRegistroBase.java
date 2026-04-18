package com.software.fixlab.entity.auditoria;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Campos comunes de auditoría HTTP por dominio. Cada módulo persiste en su propia tabla.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AuditoriaRegistroBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String usuarioEmail;

    /** Etiqueta del controlador de origen (ej. PRODUCTO, PEDIDO). */
    @Column(nullable = false, length = 80)
    private String modulo;

    @Column(nullable = false, length = 255)
    private String accion;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fechaHora;
}
