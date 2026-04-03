package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReparacionCreateReqDTO {

    @NotNull
    private Integer equipoId;

    @NotNull
    private Integer tallerId;

    @NotBlank
    private String descripcionFalla;
}
