package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class RegistroReqDTO {
    private String cedula;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
}