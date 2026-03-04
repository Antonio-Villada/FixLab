package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.TipoProductoReqDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import java.util.List;

public interface TipoProductoService {
    TipoProductoRespDTO crearTipoProducto(TipoProductoReqDTO dto) throws Exception;
    List<TipoProductoRespDTO> obtenerTodos();
}