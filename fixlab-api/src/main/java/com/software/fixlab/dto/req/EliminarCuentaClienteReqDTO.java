package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EliminarCuentaClienteReqDTO {

    @NotBlank(message = "La contraseña es obligatoria para eliminar la cuenta.")
    private String password;
}
