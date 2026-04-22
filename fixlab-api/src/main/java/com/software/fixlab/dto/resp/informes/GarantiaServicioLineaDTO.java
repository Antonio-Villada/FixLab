package com.software.fixlab.dto.resp.informes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GarantiaServicioLineaDTO {
    private Integer reparacionId;
    private String numeroTicket;
    private String clienteNombre;
    private LocalDate fechaFinGarantia;
    private String situacion;
}
