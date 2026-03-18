package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class CambiarPasswordReqDTO {
    private String contraseñaActual;
    private String nuevaPassword;
}
