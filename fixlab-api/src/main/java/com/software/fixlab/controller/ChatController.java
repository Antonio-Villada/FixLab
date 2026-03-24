package com.software.fixlab.controller;

import com.software.fixlab.dto.req.ChatEnviarReqDTO;
import com.software.fixlab.dto.resp.ChatEnviarRespDTO;
import com.software.fixlab.dto.resp.ChatMensajeRespDTO;
import com.software.fixlab.service.interfaces.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/historial")
    public List<ChatMensajeRespDTO> historial(Principal principal) {
        return chatService.historial(principal.getName());
    }

    @PostMapping("/mensaje")
    public ChatEnviarRespDTO enviar(Principal principal, @Valid @RequestBody ChatEnviarReqDTO body) {
        return chatService.enviar(principal.getName(), body);
    }

    @DeleteMapping("/historial")
    public ResponseEntity<Void> limpiar(Principal principal) {
        chatService.limpiarHistorial(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
