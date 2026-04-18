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

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

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

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    @Column(name = "expiracion_token")
    private LocalDateTime expiracionToken;

    /** Código de 6 dígitos para recuperar contraseña (envío por correo). Se reemplaza por token tras verificar. */
    @JsonIgnore
    @Column(name = "codigo_recuperacion", length = 6)
    private String codigoRecuperacion;

    @JsonIgnore
    @Column(name = "expiracion_codigo_recuperacion")
    private LocalDateTime expiracionCodigoRecuperacion;

    /**
     * Código de 6 dígitos enviado por correo en el segundo paso del login (2FA por email).
     * Null cuando no hay login pendiente de verificación.
     */
    @JsonIgnore
    @Column(name = "codigo_login_2fa", length = 6)
    private String codigoLogin2fa;

    @JsonIgnore
    @Column(name = "expiracion_codigo_login_2fa")
    private LocalDateTime expiracionCodigoLogin2fa;

    /** Intentos fallidos al validar el código de login 2FA (se resetea al éxito o al nuevo código). */
    @Column(name = "intentos_codigo_login_2fa", nullable = false)
    @Builder.Default
    private int intentosCodigoLogin2fa = 0;

    /** Último envío del código de login por correo (anti-spam si el código sigue vigente). */
    @JsonIgnore
    @Column(name = "ultimo_envio_codigo_login_2fa")
    private LocalDateTime ultimoEnvioCodigoLogin2fa;

    /**
     * Cuenta activa en la plataforma. Si es false, el cliente no puede iniciar sesión ni usar el API;
     * el registro en base de datos se conserva por trazabilidad y relaciones con pedidos, reparaciones, etc.
     */
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}