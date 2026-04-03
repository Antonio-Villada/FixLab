package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReparacionProductoReqDTO {

    @NotNull
    private Long productoId;

    @NotNull
    @Positive
    private Integer cantidad;
}
