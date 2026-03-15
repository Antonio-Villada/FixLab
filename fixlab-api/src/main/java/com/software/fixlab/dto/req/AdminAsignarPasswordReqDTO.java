package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAsignarPasswordReqDTO {
    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, incluyendo letras, números y caracteres especiales.")
    private String nuevaPassword;
}
