package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.CategoriaReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import java.util.List;

public interface CategoriaService {
    CategoriaRespDTO crearCategoria(CategoriaReqDTO dto) throws Exception;
    List<CategoriaRespDTO> obtenerTodas();
}