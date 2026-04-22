package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientesComprasReporteDTO {
    private InformeMetadatosDTO meta;
    private long clientesDistintosConCompra;
    private List<ClienteCompraLineaDTO> topClientes;
}
