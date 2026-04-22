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
public class PqrsResumenReporteDTO {
    private InformeMetadatosDTO meta;
    private long totalSolicitudes;
    private Double diasPromedioHastaCierre;
    private List<PqrTipoConteoDTO> porTipo;
    private List<PqrEstadoConteoDTO> porEstado;
}
