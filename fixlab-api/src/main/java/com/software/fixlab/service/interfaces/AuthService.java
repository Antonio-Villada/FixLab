package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.LoginReqDTO;
import com.software.fixlab.dto.req.RegistroReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;

public interface AuthService {
    MensajeRespDTO registrarCliente(RegistroReqDTO registroReqDTO) throws Exception;
    TokenRespDTO login(LoginReqDTO loginReqDTO) throws Exception;
}