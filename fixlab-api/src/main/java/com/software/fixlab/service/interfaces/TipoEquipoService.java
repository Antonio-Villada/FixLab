package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.TipoEquipoReqDTO;
import com.software.fixlab.dto.resp.TipoEquipoRespDTO;

import java.util.List;

public interface TipoEquipoService {

    TipoEquipoRespDTO crear(TipoEquipoReqDTO dto);

    List<TipoEquipoRespDTO> listarTodos();

    TipoEquipoRespDTO obtenerPorId(Integer id);

    TipoEquipoRespDTO actualizar(Integer id, TipoEquipoReqDTO dto);

    void eliminar(Integer id);
}
