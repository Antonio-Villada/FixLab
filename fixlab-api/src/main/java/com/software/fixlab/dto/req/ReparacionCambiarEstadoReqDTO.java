package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReparacionCambiarEstadoReqDTO {

    /** Nombre del enum {@link com.software.fixlab.entity.EstadoReparacion}, ej. EN_REPARACION */
    @NotBlank
    private String estadoNuevo;

    private String comentario;
}
