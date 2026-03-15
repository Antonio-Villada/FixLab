package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class UsuarioUpdateReqDTO {
    private String nombre;
    private String apellido;
    private String telefono;
    /** URL de la foto de perfil (opcional; vacío o null para no cambiar). */
    private String fotoUrl;
    // Nota: El email y la cédula normalmente no se actualizan por este medio por seguridad
}