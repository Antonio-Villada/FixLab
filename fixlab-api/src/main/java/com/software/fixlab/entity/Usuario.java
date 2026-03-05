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

    @Id
    @Column(name = "cedula", length = 20, nullable = false, unique = true)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

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