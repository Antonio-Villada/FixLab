package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuariosPorRolReporteDTO {
    private Instant generadoEn;
    private long totalUsuarios;
    private List<UsuarioPorRolLineaDTO> lineas;
}
