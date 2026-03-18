package com.software.fixlab.dto.req;
import lombok.Data;

@Data
public class ResetearPasswordDTO {
    private String token;
    private String nuevaPassword;
}