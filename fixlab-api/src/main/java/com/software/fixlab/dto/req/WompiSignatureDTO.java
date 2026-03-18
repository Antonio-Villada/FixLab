package com.software.fixlab.dto.req;

import lombok.Data;

@Data
public class WompiSignatureDTO {
    private String checksum;
    private String[] properties;
}