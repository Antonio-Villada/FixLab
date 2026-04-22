package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PedidoLogisticaLineaDTO {
    private Integer pedidoId;
    private LocalDateTime fechaCreacion;
    private String estado;
    private Double total;
    private String clienteCedula;
    private String clienteNombre;
    private String direccionEnvio;
}
