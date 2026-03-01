package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutRespDTO {
    private Long pedidoId;
    private String urlPago;
}