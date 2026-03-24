package com.software.fixlab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatEnviarReqDTO {

    @NotBlank
    @Size(max = 4000)
    private String texto;
}
