package com.software.fixlab.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WompiTransactionDTO {
    private String id;
    private String status;
    @JsonProperty("amount_in_cents")
    private Long amount_in_cents;
    private String reference;
}