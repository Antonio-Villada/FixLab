package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class RegistroReqDTO {
<<<<<<< HEAD
    private String cedula;
    private String nombre;
    private String apellido;
=======

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 5, max = 20, message = "La cédula debe tener entre 5 y 20 caracteres")
    private String cedula;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String apellidos;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String direccion;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
>>>>>>> ebc21313413cb4bf66ee58a55f8bed5b9e914097
    private String email;
    private String password;
    private String telefono;
}