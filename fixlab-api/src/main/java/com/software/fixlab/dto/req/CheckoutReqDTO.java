package com.software.fixlab.dto.req;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutReqDTO {
    private String direccionEnvio;
    private List<ItemCarritoDTO> items;
}