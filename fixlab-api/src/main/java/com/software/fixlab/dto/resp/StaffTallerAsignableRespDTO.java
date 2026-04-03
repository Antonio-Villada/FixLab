package com.software.fixlab.dto.resp;

import com.software.fixlab.entity.RolUsuario;
import lombok.Builder;
import lombok.Data;

/** Usuario que puede recibir asignación como técnico de una reparación (técnico o admin). */
@Data
@Builder
public class StaffTallerAsignableRespDTO {
    private String cedula;
    private String nombre;
    private String apellido;
    private RolUsuario rol;
}
