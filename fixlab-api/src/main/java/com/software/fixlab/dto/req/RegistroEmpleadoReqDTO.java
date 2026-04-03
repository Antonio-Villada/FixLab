package com.software.fixlab.dto.req;

import com.software.fixlab.entity.RolUsuario;
import lombok.Data;

@Data
public class RegistroEmpleadoReqDTO {
    private String cedula;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
    private RolUsuario rol; // ADMIN, TECNICO o RECEPCIONISTA
}