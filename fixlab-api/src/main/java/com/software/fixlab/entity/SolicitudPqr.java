package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes_pqr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudPqr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "radicado", nullable = false, unique = true, length = 40)
    private String radicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_cedula", referencedColumnName = "cedula", nullable = false)
    private Usuario cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_documento", nullable = false, length = 30)
    private OrigenDocumentoPqr origenDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reparacion_id")
    private Reparacion reparacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoSolicitudPqr tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ElementCollection
    @CollectionTable(name = "solicitud_pqr_evidencias", joinColumns = @JoinColumn(name = "solicitud_pqr_id"))
    @Column(name = "url", length = 1000)
    @Builder.Default
    private List<String> evidenciasUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoSolicitudPqr estado = EstadoSolicitudPqr.ABIERTO;

    @CreationTimestamp
    @Column(name = "fecha_radicacion", updatable = false)
    private LocalDateTime fechaRadicacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /** Registro interno; no exponer al cliente en API pública. */
    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @Column(name = "garantia_fisica_validada", nullable = false)
    @Builder.Default
    private boolean garantiaFisicaValidada = false;

    @Column(name = "fecha_validacion_garantia_fisica")
    private LocalDateTime fechaValidacionGarantiaFisica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_validacion_cedula", referencedColumnName = "cedula")
    private Usuario tecnicoValidacion;

    /** Indica si al radicar se verificó vigencia de garantía (solo aplica a SOLICITUD_GARANTIA). */
    @Column(name = "garantia_vigente_al_radicar", nullable = false)
    @Builder.Default
    private boolean garantiaVigenteAlRadicar = false;
}
