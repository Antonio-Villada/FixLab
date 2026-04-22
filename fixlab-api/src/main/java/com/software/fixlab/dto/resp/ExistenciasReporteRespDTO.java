package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExistenciasReporteRespDTO {
    private ExistenciasResumenRespDTO resumen;
    private List<ExistenciasLineaRespDTO> lineas;
}
