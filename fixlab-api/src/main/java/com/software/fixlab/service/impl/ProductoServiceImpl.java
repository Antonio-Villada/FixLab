package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.entity.Categoria;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteCategoriaException;
import com.software.fixlab.exception.NoExisteProductoException;
import com.software.fixlab.exception.NoExisteTipoProductoException;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.repository.DetallePedidoRepository;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen) {
        // Validación de relaciones
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new NoExisteCategoriaException("La categoría con ID " + dto.getCategoriaId() + " no existe."));

        TipoProducto tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                .orElseThrow(() -> new NoExisteTipoProductoException("El tipo de producto con ID " + dto.getTipoProductoId() + " no existe."));

        // Subida de imagen
        String urlImagenSubida;
        try {
            urlImagenSubida = cloudinaryService.subirImagen(imagen);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen a Cloudinary", e);
        }

        Producto nuevoProducto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .sku(dto.getSku())
                .imagenUrl(urlImagenSubida)
                .categoria(categoria)
                .tipoProducto(tipoProducto)
                .activo(true) // Se inicializa como activo
                .build();

        productoRepository.save(nuevoProducto);
        return mapearADto(nuevoProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespDTO> obtenerTodosLosProductos() {
        return productoRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespDTO> obtenerProductosConFiltro(String filtro, Long categoriaId) {
        if (filtro == null || filtro.isBlank() || "todos".equalsIgnoreCase(filtro.trim())) {
            return obtenerTodosLosProductos();
        }
        String f = filtro.trim().toLowerCase();
        switch (f) {
            case "mas_vendidos":
                return obtenerMasVendidos(categoriaId);
            case "pedidos_pendientes":
                return obtenerEnPedidosPorEstado("PENDIENTE");
            case "pedidos_pagados":
                return obtenerEnPedidosPorEstado("PAGADO");
            case "sin_stock":
                return productoRepository.findByStockOrderByNombre(0).stream()
                        .map(this::mapearADto)
                        .collect(Collectors.toList());
            case "inactivos":
                return productoRepository.findByActivoOrderByNombre(false).stream()
                        .map(this::mapearADto)
                        .collect(Collectors.toList());
            default:
                return obtenerTodosLosProductos();
        }
    }

    private List<ProductoRespDTO> obtenerMasVendidos(Long categoriaId) {
        List<Object[]> resultados = (categoriaId != null && categoriaId > 0)
                ? detallePedidoRepository.findProductoIdAndTotalCantidadVendidaByCategoriaId(categoriaId)
                : detallePedidoRepository.findProductoIdAndTotalCantidadVendida();
        if (resultados.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> idsOrdenados = resultados.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
        Map<Long, Integer> cantidadPorProducto = new LinkedHashMap<>();
        for (Object[] row : resultados) {
            cantidadPorProducto.put((Long) row[0], ((Number) row[1]).intValue());
        }
        List<Producto> productos = productoRepository.findAllById(idsOrdenados);
        Map<Long, Producto> porId = productos.stream().collect(Collectors.toMap(Producto::getId, p -> p));
        List<ProductoRespDTO> dtos = new ArrayList<>();
        for (Long id : idsOrdenados) {
            Producto p = porId.get(id);
            if (p != null) {
                ProductoRespDTO dto = mapearADto(p);
                dto.setCantidadVendida(cantidadPorProducto.getOrDefault(id, 0));
                dtos.add(dto);
            }
        }
        return dtos;
    }

    private List<ProductoRespDTO> obtenerEnPedidosPorEstado(String estado) {
        List<Long> ids = detallePedidoRepository.findDistinctProductoIdsByPedidoEstado(estado);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return productoRepository.findByIdInOrderByNombre(ids).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoRespDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoExisteProductoException("Producto no encontrado con ID: " + id));
        return mapearADto(producto);
    }

    @Override
    @Transactional
    public ProductoRespDTO actualizarProducto(Long id, ProductoReqDTO dto, MultipartFile imagen) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoExisteProductoException("Producto no encontrado con ID: " + id));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new NoExisteCategoriaException("La categoría con ID " + dto.getCategoriaId() + " no existe."));

        TipoProducto tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                .orElseThrow(() -> new NoExisteTipoProductoException("El tipo de producto con ID " + dto.getTipoProductoId() + " no existe."));

        // Actualizar datos básicos
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setSku(dto.getSku());
        producto.setCategoria(categoria);
        producto.setTipoProducto(tipoProducto);

        // Si se envió una nueva imagen, se sube y se actualiza la URL
        if (imagen != null && !imagen.isEmpty()) {
            try {
                String nuevaUrl = cloudinaryService.subirImagen(imagen);
                producto.setImagenUrl(nuevaUrl);
            } catch (Exception e) {
                throw new RuntimeException("Error al subir la nueva imagen a Cloudinary", e);
            }
        }

        productoRepository.save(producto);
        return mapearADto(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NoExisteProductoException("Producto no encontrado con ID: " + id));

        // Soft delete: en lugar de borrarlo físicamente, lo desactivamos
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoRespDTO mapearADto(Producto producto) {
        return ProductoRespDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .sku(producto.getSku())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .categoria(CategoriaRespDTO.builder()
                        .id(producto.getCategoria().getId())
                        .nombre(producto.getCategoria().getNombre())
                        .build())
                .tipoProducto(TipoProductoRespDTO.builder()
                        .id(producto.getTipoProducto().getId())
                        .nombre(producto.getTipoProducto().getNombre())
                        .build())
                .build();
    }
}