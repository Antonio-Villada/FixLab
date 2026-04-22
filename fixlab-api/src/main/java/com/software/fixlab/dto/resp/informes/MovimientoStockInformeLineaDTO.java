package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoStockInformeLineaDTO {
    private Long id;
    private Instant fechaRegistro;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Integer cantidad;
    private String comentario;
}
