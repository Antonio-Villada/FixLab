package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRespDTO {
    private String token;
    private String rol;
    /** Si true, el front debe obligar a cambiar la contraseña antes de usar la app. */
    private boolean requiereCambioPassword;
}