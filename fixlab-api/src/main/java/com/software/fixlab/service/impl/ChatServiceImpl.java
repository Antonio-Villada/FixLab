package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ChatEnviarReqDTO;
import com.software.fixlab.dto.resp.ChatEnviarRespDTO;
import com.software.fixlab.dto.resp.ChatMensajeRespDTO;
import com.software.fixlab.entity.ChatMensaje;
import com.software.fixlab.entity.ChatRol;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.ChatMensajeRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.ChatService;
import com.software.fixlab.util.ChatHybridResponder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMensajeRepository chatMensajeRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChatMensajeRespDTO> historial(String usuarioEmail) {
        String email = emailCanonico(usuarioEmail);
        return chatMensajeRepository.findByUsuarioEmailOrderByCreadoEnAsc(email).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ChatEnviarRespDTO enviar(String usuarioEmail, ChatEnviarReqDTO body) {
        String email = emailCanonico(usuarioEmail);
        String t = body.getTexto().trim();
        if (t.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje no puede estar vacío");
        }

        ChatMensaje user = ChatMensaje.builder()
                .usuarioEmail(email)
                .texto(t)
                .rol(ChatRol.USER)
                .build();
        user = chatMensajeRepository.save(user);

        String replyText = ChatHybridResponder.reply(t);
        ChatMensaje bot = ChatMensaje.builder()
                .usuarioEmail(email)
                .texto(replyText)
                .rol(ChatRol.BOT)
                .build();
        bot = chatMensajeRepository.save(bot);

        return ChatEnviarRespDTO.builder()
                .userMessage(toDto(user))
                .botMessage(toDto(bot))
                .build();
    }

    @Override
    @Transactional
    public void limpiarHistorial(String usuarioEmail) {
        String email = emailCanonico(usuarioEmail);
        chatMensajeRepository.deleteByUsuarioEmail(email);
    }

    private String emailCanonico(String emailJwt) {
        return usuarioRepository.findByEmailIgnoreCase(emailJwt.trim())
                .map(Usuario::getEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no encontrado"));
    }

    private ChatMensajeRespDTO toDto(ChatMensaje m) {
        return ChatMensajeRespDTO.builder()
                .id(m.getId())
                .role(m.getRol())
                .text(m.getTexto())
                .createdAt(m.getCreadoEn())
                .build();
    }
}
