package com.software.fixlab.dto.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WompiCheckoutDTO {
    private Integer pedidoId;
    private String referencia;
    private Long montoEnCentavos;
    private String moneda;
    private String firmaIntegridad;
    private String llavePublica;
}