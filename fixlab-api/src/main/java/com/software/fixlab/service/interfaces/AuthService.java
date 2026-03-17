package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    MensajeRespDTO registrarCliente(RegistroReqDTO dto) throws Exception;
    MensajeRespDTO registrarClienteConFoto(RegistroReqDTO dto, org.springframework.web.multipart.MultipartFile foto) throws Exception;
    MensajeRespDTO registrarEmpleado(RegistroEmpleadoReqDTO dto) throws Exception;
    MensajeRespDTO login(LoginReqDTO dto) throws Exception;
    TokenRespDTO verificarCodigoLogin(VerificarCorreoReqDTO dto) throws Exception;
    MensajeRespDTO cambiarRol(CambioRolReqDTO dto) throws Exception;

    @Transactional
    MensajeRespDTO verificarCorreo(VerificarCorreoReqDTO dto) throws Exception;
    void solicitarRecuperacionPassword(String email) throws Exception;
    void cambiarPasswordConToken(String token, String nuevaPassword) throws Exception;
    /** Cambia la contraseña del usuario autenticado (requiere contraseña actual). */
    void cambiarPassword(String email, String contraseñaActual, String nuevaPassword) throws Exception;

    /** Asigna una nueva contraseña a un usuario por cédula (solo ADMIN, p. ej. usuario olvidó contraseña). */
    void asignarNuevaPasswordPorCedula(String cedula, String nuevaPassword) throws Exception;
}