package com.software.fixlab.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellidos;

    // Regla de negocio: El correo debe ser único en el sistema.
    @Column(nullable = false, unique = true, length = 150)
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

    // --- Control de seguridad: Bloqueo tras 3 intentos fallidos ---

    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;
}