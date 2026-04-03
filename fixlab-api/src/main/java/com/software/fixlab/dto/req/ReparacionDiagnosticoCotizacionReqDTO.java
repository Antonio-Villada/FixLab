package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class ReparacionDiagnosticoCotizacionReqDTO {

    private String diagnostico;
    private Double cotizacionTotal;
}
