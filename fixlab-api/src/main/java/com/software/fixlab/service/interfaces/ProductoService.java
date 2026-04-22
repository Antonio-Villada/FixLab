package com.software.fixlab.service.interfaces;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.software.fixlab.dto.req.EntradaMercanciaReqDTO;
import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.EntradaMercanciaRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;

public interface ProductoService {
    ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen);
    List<ProductoRespDTO> obtenerTodosLosProductos();
    ProductoRespDTO obtenerPorId(Long id);
    ProductoRespDTO actualizarProducto(Long id, ProductoReqDTO dto, MultipartFile imagen);
    void eliminarProducto(Long id);

    EntradaMercanciaRespDTO registrarEntradaMercancia(Long productoId, EntradaMercanciaReqDTO dto);

    List<ProductoRespDTO> listarProductosConStockBajo();

    List<EntradaMercanciaRespDTO> listarEntradasMercancia(Long productoId);
}