package com.software.fixlab.dto.req;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

@Data
public class VerificarCodigoRecuperacionReqDTO {
    private String email;
    private String codigo;

    @JsonSetter
    public void setCodigo(Object codigo) {
        this.codigo = codigo != null ? String.valueOf(codigo).trim() : null;
    }
}
