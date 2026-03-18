package com.software.fixlab.dto.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetallePedidoRespDTO {
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}