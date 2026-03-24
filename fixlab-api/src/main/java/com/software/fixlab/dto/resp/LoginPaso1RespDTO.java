package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Respuesta del primer paso del login: credenciales correctas; falta el código del correo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginPaso1RespDTO {

    /** Siempre {@code CODIGO_ENVIADO} en este paso. */
    private String paso;

    /** Correo enmascarado para mostrar al usuario (ej. i***@gmail.com). */
    private String emailMascarado;
}
