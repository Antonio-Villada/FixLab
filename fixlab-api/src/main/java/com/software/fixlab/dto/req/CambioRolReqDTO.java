package com.software.fixlab.dto.req;

import com.software.fixlab.entity.RolUsuario;
import lombok.Data;

@Data
public class CambioRolReqDTO {
    private String cedula;
    private RolUsuario nuevoRol;
}