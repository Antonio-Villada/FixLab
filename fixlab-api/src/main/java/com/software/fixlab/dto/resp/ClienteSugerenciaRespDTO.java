package com.software.fixlab.dto.resp;

import lombok.Builder;
import lombok.Data;

/** Respuesta mínima para autocompletado de cliente en recepción (solo cédula y nombre visible). */
@Data
@Builder
public class ClienteSugerenciaRespDTO {
    private String cedula;
    private String nombre;
    private String apellido;
}
