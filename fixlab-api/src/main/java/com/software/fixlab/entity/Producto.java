package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    // Regla de calidad: El stock no puede ser negativo
    @Column(nullable = false)
    private Integer stock;

    /**
     * Umbral para alertas de stock bajo (productos activos con stock en o por debajo de este valor).
     * Si es null en BD legada, el backend trata 5 como valor por defecto.
     */
    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    // Aquí guardaremos el link de Cloudinary
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activo; // Para no borrar productos si hay facturas asociadas (Soft Delete)

    // Relación con Categoría (Ej: Repuestos, Accesorios, Herramientas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Relación con Tipo de Producto (Ej: Pantallas, Baterías, Cargadores)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_producto_id", nullable = false)
    private TipoProducto tipoProducto;

}