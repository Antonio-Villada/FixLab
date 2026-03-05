package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;

import java.util.List;

public interface UsuarioService {
    // Read
    List<UsuarioRespDTO> obtenerTodos();
    UsuarioRespDTO obtenerPorCedula(String cedula);

    // Update
    UsuarioRespDTO actualizarUsuario(String cedula, UsuarioUpdateReqDTO dto);

    // Delete
    void eliminarUsuario(String cedula);
}