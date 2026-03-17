package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMensajeReqDTO {
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 2000)
    private String mensaje;
}
