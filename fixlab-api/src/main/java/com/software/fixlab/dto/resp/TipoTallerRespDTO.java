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
public class TipoTallerRespDTO {
    private Integer id;
    private String nombre;
    private String ciclo;
    private String estado;
    private LocalDateTime fechaCreacion;
}
