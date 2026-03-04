package com.software.fixlab.dto.req;
import lombok.Data;

@Data
public class VerificarCorreoReqDTO {
    private String email;
    private String codigo;
}