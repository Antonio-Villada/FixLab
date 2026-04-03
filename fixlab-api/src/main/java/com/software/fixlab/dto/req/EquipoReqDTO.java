package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipoReqDTO {

    @NotNull
    private Integer tipoEquipoId;

    private String marca;
    private String modelo;
    private String numeroSerie;
    private String observaciones;

    /**
     * Obligatorio si el actor es ADMIN, TECNICO o RECEPCIONISTA: cédula del cliente dueño del equipo.
     * Ignorado si el actor es CLIENTE (el dueño es quien inicia sesión).
     */
    private String propietarioCedula;
}
