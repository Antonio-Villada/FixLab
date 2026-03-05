package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.CategoriaReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import java.util.List;

public interface CategoriaService {
    CategoriaRespDTO crearCategoria(CategoriaReqDTO dto);
    List<CategoriaRespDTO> obtenerTodas();
    CategoriaRespDTO obtenerPorId(Integer id);
    CategoriaRespDTO actualizarCategoria(Integer id, CategoriaReqDTO dto);
    void eliminarCategoria(Integer id);
}