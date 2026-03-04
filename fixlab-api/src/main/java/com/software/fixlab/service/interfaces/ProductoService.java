package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {
    // Agregamos el MultipartFile a la firma del método
    ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen) throws Exception;
    List<ProductoRespDTO> obtenerTodosLosProductos();
}