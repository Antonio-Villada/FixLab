package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReparacionEvidenciaReqDTO {

    @NotBlank
    private String url;

    /** Valor del enum {@link com.software.fixlab.entity.TipoEvidenciaReparacion}, ej. RECEPCION */
    @NotNull
    private String tipo;

    private Integer orden;
}
