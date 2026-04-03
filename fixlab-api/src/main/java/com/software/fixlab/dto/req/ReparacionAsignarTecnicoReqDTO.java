package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReparacionAsignarTecnicoReqDTO {

    /** Cédula del usuario con rol TECNICO (o ADMIN si la regla de negocio lo permite). */
    @NotBlank
    private String tecnicoCedula;
}
