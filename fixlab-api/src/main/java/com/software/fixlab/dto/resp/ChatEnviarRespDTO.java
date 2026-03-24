package com.software.fixlab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEnviarRespDTO {
    private ChatMensajeRespDTO userMessage;
    private ChatMensajeRespDTO botMessage;
}
