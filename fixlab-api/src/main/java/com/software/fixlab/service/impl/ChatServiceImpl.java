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
    private final GeminiChatClient geminiChatClient;

    @Override
    @Transactional(readOnly = true)
    public List<ChatMensajeRespDTO> historial(String usuarioEmail) {
        String email = resolveUsuario(usuarioEmail).getEmail();
        return chatMensajeRepository.findByUsuarioEmailOrderByCreadoEnAsc(email).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ChatEnviarRespDTO enviar(String usuarioEmail, ChatEnviarReqDTO body) {
        Usuario usuario = resolveUsuario(usuarioEmail);
        String email = usuario.getEmail();
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

        List<ChatMensaje> historialAsc = chatMensajeRepository.findByUsuarioEmailOrderByCreadoEnAsc(email);
        var iaReply = geminiChatClient.generateReply(historialAsc, usuario.getRol()).filter(s -> !s.isBlank());
        String replyText = iaReply.orElseGet(() -> ChatHybridResponder.reply(t, usuario.getRol()));
        String respuestaFuente = iaReply.isPresent() ? "IA" : "FAQ";
        ChatMensaje bot = ChatMensaje.builder()
                .usuarioEmail(email)
                .texto(replyText)
                .rol(ChatRol.BOT)
                .build();
        bot = chatMensajeRepository.save(bot);

        return ChatEnviarRespDTO.builder()
                .userMessage(toDto(user))
                .botMessage(toDto(bot))
                .respuestaFuente(respuestaFuente)
                .build();
    }

    @Override
    @Transactional
    public void limpiarHistorial(String usuarioEmail) {
        String email = resolveUsuario(usuarioEmail).getEmail();
        chatMensajeRepository.deleteByUsuarioEmail(email);
    }

    private Usuario resolveUsuario(String emailJwt) {
        return usuarioRepository.findByEmailIgnoreCase(emailJwt.trim())
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
