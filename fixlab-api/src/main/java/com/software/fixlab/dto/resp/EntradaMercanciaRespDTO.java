package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntradaMercanciaRespDTO {
    private Long id;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Integer cantidad;
    private Integer nuevoStock;
    private String comentario;
    private Instant fechaRegistro;
}
