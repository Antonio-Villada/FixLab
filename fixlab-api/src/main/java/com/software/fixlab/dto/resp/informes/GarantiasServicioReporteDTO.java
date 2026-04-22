package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarantiasServicioReporteDTO {
    private Instant generadoEn;
    private int diasVentana;
    private long vencidas;
    private long proximasAVencer;
    private List<GarantiaServicioLineaDTO> lineas;
}
