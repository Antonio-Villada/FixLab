package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.resp.ChatRespuestaRespDTO;

public interface ChatService {
    /**
     * Responde al mensaje del usuario: primero intenta reglas/FAQ (pedidos, productos, contacto);
     * si no aplica, delega en IA (OpenAI) si está configurada.
     */
    ChatRespuestaRespDTO responder(String mensajeUsuario, String emailUsuario);
}
