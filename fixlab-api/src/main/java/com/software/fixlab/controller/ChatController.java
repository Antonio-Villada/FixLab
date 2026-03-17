package com.software.fixlab.controller;

import com.software.fixlab.dto.req.ChatMensajeReqDTO;
import com.software.fixlab.dto.resp.ChatRespuestaRespDTO;
import com.software.fixlab.service.interfaces.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/mensaje")
    public ResponseEntity<ChatRespuestaRespDTO> enviarMensaje(
            @RequestBody @Valid ChatMensajeReqDTO dto,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        ChatRespuestaRespDTO respuesta = chatService.responder(dto.getMensaje(), email);
        return ResponseEntity.ok(respuesta);
    }
}
