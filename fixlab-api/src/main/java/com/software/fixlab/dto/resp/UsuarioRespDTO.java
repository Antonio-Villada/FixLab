package com.software.fixlab.dto.resp;

import com.software.fixlab.entity.RolUsuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioRespDTO {
    private String cedula;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    /** URL de la foto de perfil (ej. Cloudinary). */
    private String fotoUrl;
    private RolUsuario rol;
    private boolean correoVerificado;
}