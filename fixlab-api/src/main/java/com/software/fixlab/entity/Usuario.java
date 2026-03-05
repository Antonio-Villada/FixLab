package com.software.fixlab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    /** Identificación del usuario (cédula). No es autoincrementable; la ingresa el usuario. */
    @Id
<<<<<<< HEAD
    @Column(name = "cedula", length = 20, nullable = false, unique = true)
=======
    @Column(nullable = false, unique = true, length = 20)
>>>>>>> ebc21313413cb4bf66ee58a55f8bed5b9e914097
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
<<<<<<< HEAD
    private String apellido;

    @Column(nullable = false, unique = true)
=======
    private String apellidos;

    // Regla de negocio: El correo debe ser único en el sistema.
    @Column(nullable = false, unique = true, length = 150)
>>>>>>> ebc21313413cb4bf66ee58a55f8bed5b9e914097
    private String email;

    @Column(length = 255)
    private String direccion;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    // Control de seguridad
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    // ... otros campos ...
    @Column(nullable = false)
    private boolean correoVerificado;

    // --- NUEVOS CAMPOS PARA EL CÓDIGO DE VERIFICACIÓN ---
    @Column(length = 6)
    private String codigoVerificacion;

    private LocalDateTime expiracionCodigo;
}