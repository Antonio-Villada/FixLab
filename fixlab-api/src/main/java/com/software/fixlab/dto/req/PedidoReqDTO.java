package com.software.fixlab.dto.req;

import lombok.Data;
import java.util.List;

@Data
public class PedidoReqDTO {
    private String direccionEnvio; // A dónde le mandamos los repuestos
    private List<ItemCarritoDTO> items; // La lista de todo lo que compró
}