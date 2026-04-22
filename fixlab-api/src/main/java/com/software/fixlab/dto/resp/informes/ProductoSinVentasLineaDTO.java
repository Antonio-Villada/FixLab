package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoSinVentasLineaDTO {
    private Long productoId;
    private String sku;
    private String nombre;
    private String categoriaNombre;
    private String tipoProductoNombre;
    private Integer stock;
    private Boolean activo;
}
