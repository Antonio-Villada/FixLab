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
public class VentasPorCategoriaReporteDTO {
    private InformeMetadatosDTO meta;
    private Double montoTotalPagado;
    private List<VentasPorCategoriaLineaDTO> lineas;
}
