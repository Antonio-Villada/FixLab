package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reparacion_historial_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReparacionHistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reparacion_id", nullable = false)
    private Reparacion reparacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 40)
    private EstadoReparacion estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private EstadoReparacion estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cedula", referencedColumnName = "cedula")
    private Usuario usuario;

    @Column(length = 500)
    private String comentario;

    @CreationTimestamp
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;
}
