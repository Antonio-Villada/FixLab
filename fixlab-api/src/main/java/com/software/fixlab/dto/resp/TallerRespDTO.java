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
public class TallerRespDTO {
    private Integer id;
    private String nombre;
    private Integer tipoTallerId;
    private String tipoTallerNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
