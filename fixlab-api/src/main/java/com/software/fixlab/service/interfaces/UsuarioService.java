package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsuarioService {
    // Read
    List<UsuarioRespDTO> obtenerTodos();
    UsuarioRespDTO obtenerPorCedula(String cedula);
    UsuarioRespDTO obtenerPorEmail(String email);

    // Update
    UsuarioRespDTO actualizarUsuario(String cedula, UsuarioUpdateReqDTO dto);
    /** Actualiza el perfil del usuario autenticado (por email). */
    UsuarioRespDTO actualizarMiPerfil(String email, UsuarioUpdateReqDTO dto);
    /** Sube la foto de perfil del usuario autenticado. Retorna el perfil actualizado. */
    UsuarioRespDTO subirFotoPerfil(String email, MultipartFile foto) throws Exception;

    // Delete
    void eliminarUsuario(String cedula);
}