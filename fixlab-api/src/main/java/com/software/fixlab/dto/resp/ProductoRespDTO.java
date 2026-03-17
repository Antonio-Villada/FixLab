package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRespDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String sku;
    private String imagenUrl;
    private Boolean activo;

    /** Cantidad total vendida (solo presente cuando se listan con filtro mas_vendidos). */
    private Integer cantidadVendida;

    private CategoriaRespDTO categoria;
    private TipoProductoRespDTO tipoProducto;
}