package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatEnviarReqDTO {

    @NotBlank
    @Size(max = 4000)
    private String texto;

    /** Ruta actual del SPA (ej. /carrito) para contextualizar la respuesta. */
    @Size(max = 500)
    private String rutaApp;

    /** Resumen no sensible del carrito en el cliente (ej. unidades y subtotal). */
    @Size(max = 300)
    private String resumenCarrito;
}
