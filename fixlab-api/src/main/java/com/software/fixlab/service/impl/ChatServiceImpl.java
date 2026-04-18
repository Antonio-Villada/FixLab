package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ChatEnviarReqDTO;
import com.software.fixlab.dto.resp.ChatEnviarRespDTO;
import com.software.fixlab.dto.resp.ChatMensajeRespDTO;
import com.software.fixlab.entity.ChatMensaje;
import com.software.fixlab.entity.ChatRol;
import com.software.fixlab.entity.Pedido;
import com.software.fixlab.entity.Reparacion;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.ChatMensajeRepository;
import com.software.fixlab.repository.PedidoRepository;
import com.software.fixlab.repository.ReparacionRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.ChatService;
import com.software.fixlab.util.ChatHybridResponder;
import com.software.fixlab.util.ChatReplySanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ChatMensajeRepository chatMensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ReparacionRepository reparacionRepository;
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
        String contextoTurno = buildTurnContext(usuario, body);
        var iaReply = geminiChatClient
                .generateReply(historialAsc, usuario.getRol(), contextoTurno)
                .filter(s -> !s.isBlank());
        String replyText = iaReply.orElseGet(() -> ChatHybridResponder.reply(t, usuario.getRol(), contextoTurno));
        replyText = ChatReplySanitizer.stripLeakedContextEcho(replyText);
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

    /**
     * Contexto solo para esta respuesta (no se guarda en mensajes): pantalla en el SPA, carrito local y,
     * si es cliente, pedidos y reparaciones recientes desde BD.
     */
    private String buildTurnContext(Usuario usuario, ChatEnviarReqDTO body) {
        StringBuilder sb = new StringBuilder();
        if (body.getRutaApp() != null && !body.getRutaApp().isBlank()) {
            sb.append("Ruta en la app web: ").append(body.getRutaApp().trim()).append('\n');
        }
        if (body.getResumenCarrito() != null && !body.getResumenCarrito().isBlank()) {
            sb.append("Carrito en el navegador: ").append(body.getResumenCarrito().trim()).append('\n');
        }
        if (usuario.getRol() == RolUsuario.CLIENTE) {
            appendClienteDatosCuenta(sb, usuario.getCedula());
        }
        return sb.toString().trim();
    }

    private void appendClienteDatosCuenta(StringBuilder sb, String cedula) {
        List<Pedido> pedidos = pedidoRepository.findByCliente_CedulaOrderByFechaCreacionDesc(cedula);
        List<Reparacion> reps = reparacionRepository.findByCliente_CedulaOrderByFechaCreacionDesc(cedula);
        sb.append("Rol en FixLab: cliente.\n");
        if (pedidos.isEmpty() && reps.isEmpty()) {
            sb.append("Pedidos registrados: ninguno aún.\n");
            sb.append("Reparaciones/taller registradas: ninguna aún.\n");
            return;
        }
        if (!pedidos.isEmpty()) {
            sb.append("Últimos pedidos (máx. 3, datos reales):\n");
            pedidos.stream().limit(3).forEach(p -> sb.append("- #")
                    .append(p.getId())
                    .append(", estado ")
                    .append(p.getEstado())
                    .append(", fecha ")
                    .append(p.getFechaCreacion() != null ? p.getFechaCreacion().toLocalDate().format(FECHA_CORTA) : "?")
                    .append(", total ")
                    .append(p.getTotal())
                    .append('\n'));
        } else {
            sb.append("Pedidos registrados: ninguno.\n");
        }
        if (!reps.isEmpty()) {
            sb.append("Últimas reparaciones (máx. 3):\n");
            reps.stream().limit(3).forEach(r -> sb.append("- Ticket ")
                    .append(r.getNumeroTicket())
                    .append(", estado ")
                    .append(r.getEstado())
                    .append(", creada ")
                    .append(r.getFechaCreacion() != null ? r.getFechaCreacion().toLocalDate().format(FECHA_CORTA) : "?")
                    .append('\n'));
        } else {
            sb.append("Reparaciones/taller registradas: ninguna.\n");
        }
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
