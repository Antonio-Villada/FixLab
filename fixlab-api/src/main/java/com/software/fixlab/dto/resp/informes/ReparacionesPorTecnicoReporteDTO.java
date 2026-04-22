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
public class ReparacionesPorTecnicoReporteDTO {
    private InformeMetadatosDTO meta;
    private List<ReparacionPorTecnicoLineaDTO> lineas;
}
