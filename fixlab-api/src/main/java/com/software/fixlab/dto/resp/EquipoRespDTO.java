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
public class EquipoRespDTO {
    private Integer id;
    private Integer tipoEquipoId;
    private String tipoEquipoNombre;
    private String clienteCedula;
    private String clienteNombre;
    private String clienteApellido;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
