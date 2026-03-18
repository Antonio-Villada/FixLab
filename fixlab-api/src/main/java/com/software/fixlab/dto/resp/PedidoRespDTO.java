package com.software.fixlab.dto.resp;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoRespDTO {
    private Integer id;
    private LocalDateTime fechaCreacion;
    private Double total;
    private String estado;
    private String clienteCedula;
    private String clienteNombre;
    private String direccionEnvio;
    private List<DetallePedidoRespDTO> detalles;
}