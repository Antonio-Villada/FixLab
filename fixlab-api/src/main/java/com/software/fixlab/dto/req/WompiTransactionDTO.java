package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class WompiTransactionDTO {
    private String id;
    private String status;
    private Long amount_in_cents;
    private String reference;
}