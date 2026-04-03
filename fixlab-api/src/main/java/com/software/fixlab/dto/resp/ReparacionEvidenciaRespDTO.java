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
public class ReparacionEvidenciaRespDTO {
    private Integer id;
    private String url;
    private String tipo;
    private Integer orden;
    private LocalDateTime fechaCreacion;
}
