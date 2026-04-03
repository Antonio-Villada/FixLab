package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.ClienteSugerenciaRespDTO;
import com.software.fixlab.dto.resp.StaffTallerAsignableRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsuarioService {
    // Read
    List<UsuarioRespDTO> obtenerTodos();
    /** Clientes (rol CLIENTE) cuya cédula contiene el fragmento (máx. 20, mín. 2 caracteres en servicio). */
    List<ClienteSugerenciaRespDTO> buscarSugerenciasClientesPorCedula(String fragmento);
    /** Técnicos y administradores que pueden asignarse a una orden (recepción / gestión taller). */
    List<StaffTallerAsignableRespDTO> listarStaffAsignableComoTecnico();
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