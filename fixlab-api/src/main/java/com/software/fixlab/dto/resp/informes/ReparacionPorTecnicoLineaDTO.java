package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReparacionPorTecnicoLineaDTO {
    private String tecnicoCedula;
    private String tecnicoNombre;
    private long reparacionesEntregadas;
    private long reparacionesActivasOtras;
}
