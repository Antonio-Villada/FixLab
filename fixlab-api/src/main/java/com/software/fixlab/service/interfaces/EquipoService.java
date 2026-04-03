package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.EquipoReqDTO;
import com.software.fixlab.dto.resp.EquipoRespDTO;
import com.software.fixlab.entity.RolUsuario;

import java.util.List;

public interface EquipoService {

    EquipoRespDTO crear(EquipoReqDTO dto, String emailUsuario, RolUsuario rol);

    List<EquipoRespDTO> listar(String emailUsuario, RolUsuario rol);

    EquipoRespDTO obtenerPorId(Integer id, String emailUsuario, RolUsuario rol);
}
