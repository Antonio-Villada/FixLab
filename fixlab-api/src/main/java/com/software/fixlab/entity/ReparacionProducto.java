package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reparacion_producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReparacionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reparacion_id", nullable = false)
    private Reparacion reparacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_snapshot", nullable = false)
    private Double precioUnitarioSnapshot;
}
