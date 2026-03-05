package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    MensajeRespDTO registrarCliente(RegistroReqDTO dto) throws Exception;

    // Nuevo método para los empleados
    MensajeRespDTO registrarEmpleado(RegistroEmpleadoReqDTO dto) throws Exception;

    TokenRespDTO login(LoginReqDTO dto) throws Exception;

    // ... otros métodos ...

    // Nuevo método para ascender o cambiar roles
    MensajeRespDTO cambiarRol(CambioRolReqDTO dto) throws Exception;

    @Transactional
    MensajeRespDTO verificarCorreo(VerificarCorreoReqDTO dto) throws Exception;
}