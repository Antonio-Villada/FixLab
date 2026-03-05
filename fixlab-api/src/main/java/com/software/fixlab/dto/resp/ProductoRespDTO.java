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
    private Long id; // <-- Cambiado a Long para coincidir con tu BD
    private String nombre;
    private String descripcion;
    private Double precio; // <-- Cambiado a Double
    private Integer stock;
    private String sku;
    private String imagenUrl;

    private CategoriaRespDTO categoria;
    private TipoProductoRespDTO tipoProducto;
}