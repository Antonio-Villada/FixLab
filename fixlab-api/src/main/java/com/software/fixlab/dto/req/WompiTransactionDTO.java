package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class WompiTransactionDTO {
    private String id;
    private Long amount_in_cents;
    private String reference;
    private String status; // APPROVED, DECLINED, VOIDED
    private String payment_method_type;
}