package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reparaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reparacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_ticket", nullable = false, unique = true, length = 30)
    private String numeroTicket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_cedula", referencedColumnName = "cedula", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "taller_id", nullable = false)
    private Taller taller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_asignado_cedula", referencedColumnName = "cedula")
    private Usuario tecnicoAsignado;

    @Column(name = "descripcion_falla", nullable = false, columnDefinition = "TEXT")
    private String descripcionFalla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private EstadoReparacion estado = EstadoReparacion.RECIBIDO;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "cotizacion_total")
    private Double cotizacionTotal;

    @Column(name = "fecha_diagnostico")
    private LocalDateTime fechaDiagnostico;

    @Column(name = "aprobado_cliente", nullable = false)
    @Builder.Default
    private boolean aprobadoCliente = false;

    @Column(name = "fecha_aprobacion_cliente")
    private LocalDateTime fechaAprobacionCliente;

    @Column(name = "meses_garantia_servicio")
    private Integer mesesGarantiaServicio;

    @Column(name = "fecha_fin_garantia_servicio")
    private LocalDate fechaFinGarantiaServicio;

    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
