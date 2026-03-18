package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRespDTO {
    private String token;
    private String rol;
}