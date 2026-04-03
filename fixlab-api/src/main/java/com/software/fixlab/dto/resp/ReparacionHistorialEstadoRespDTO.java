package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReparacionHistorialEstadoRespDTO {
    private Long id;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioCedula;
    private String usuarioNombre;
    private String usuarioApellido;
    private String comentario;
    private LocalDateTime fechaCambio;
}
