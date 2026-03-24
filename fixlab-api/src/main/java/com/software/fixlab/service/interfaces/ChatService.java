package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.ChatEnviarReqDTO;
import com.software.fixlab.dto.resp.ChatEnviarRespDTO;
import com.software.fixlab.dto.resp.ChatMensajeRespDTO;

import java.util.List;

public interface ChatService {

    List<ChatMensajeRespDTO> historial(String usuarioEmail);

    ChatEnviarRespDTO enviar(String usuarioEmail, ChatEnviarReqDTO body);

    void limpiarHistorial(String usuarioEmail);
}
