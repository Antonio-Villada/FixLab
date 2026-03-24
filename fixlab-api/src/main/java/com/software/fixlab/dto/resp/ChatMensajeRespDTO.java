package com.software.fixlab.dto.resp;

import com.software.fixlab.entity.ChatRol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMensajeRespDTO {
    private Long id;
    private ChatRol role;
    private String text;
    private LocalDateTime createdAt;
}
