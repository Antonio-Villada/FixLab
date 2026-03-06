package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class WompiWebhookDTO {
    private String event;
    private WompiDataDTO data;
    private String environment;
    private WompiSignatureDTO signature;
    private Long timestamp;
}