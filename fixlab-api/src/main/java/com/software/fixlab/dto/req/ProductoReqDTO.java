package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class ProductoReqDTO {
    private String nombre;
    private String descripcion;
    private Double precio; // <-- Cambiado a Double para coincidir con tu BD
    private Integer stock;
    private String sku;
    private String imagenUrl;

    private Integer categoriaId;
    private Integer tipoProductoId;
}