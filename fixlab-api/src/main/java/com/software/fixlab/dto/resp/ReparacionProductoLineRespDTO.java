package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReparacionProductoLineRespDTO {
    private Integer id;
    private Long productoId;
    private String nombreProducto;
    private String sku;
    private Integer cantidad;
    private Double precioUnitarioSnapshot;
    private Double subtotal;
}
