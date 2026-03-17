package com.software.fixlab.service.interfaces;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {
    ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen);
    List<ProductoRespDTO> obtenerTodosLosProductos();
    List<ProductoRespDTO> obtenerProductosConFiltro(String filtro, Long categoriaId);
    ProductoRespDTO obtenerPorId(Long id);
    ProductoRespDTO actualizarProducto(Long id, ProductoReqDTO dto, MultipartFile imagen);
    void eliminarProducto(Long id);
}