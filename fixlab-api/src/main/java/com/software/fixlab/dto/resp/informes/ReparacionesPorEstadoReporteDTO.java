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
public class ReparacionesPorEstadoReporteDTO {
    private InformeMetadatosDTO meta;
    private long totalReparacionesPeriodo;
    private List<ReparacionPorEstadoLineaDTO> lineas;
}
