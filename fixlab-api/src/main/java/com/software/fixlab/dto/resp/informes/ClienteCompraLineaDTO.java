package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClienteCompraLineaDTO {
    private String clienteCedula;
    private String clienteNombre;
    private long pedidosPagados;
    private Double montoTotal;
}
