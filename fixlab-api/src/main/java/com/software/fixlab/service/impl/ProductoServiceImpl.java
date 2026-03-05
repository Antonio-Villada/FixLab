package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.entity.Categoria;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.ProductoService;
// IMPORTANTE: Asegúrate de importar aquí tu servicio real de Cloudinary
import com.software.fixlab.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final CloudinaryService cloudinaryService; // <-- Inyectamos tu servicio de imágenes

    @Override
    @Transactional
    public ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen) throws Exception {

        // 1. Validamos que la categoría y el tipo existan
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("La categoría con ID " + dto.getCategoriaId() + " no existe."));

        TipoProducto tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                .orElseThrow(() -> new Exception("El tipo de producto con ID " + dto.getTipoProductoId() + " no existe."));

        // 2. Subimos la imagen a Cloudinary y obtenemos la URL segura
        // (Si tu método devuelve un Map en lugar de un String, cámbialo a: cloudinaryService.subirImagen(imagen).get("url").toString();)
        String urlImagenSubida = cloudinaryService.subirImagen(imagen);

        // 3. Construimos el producto con la URL real
        Producto nuevoProducto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .sku(dto.getSku())
                .imagenUrl(urlImagenSubida) // <-- URL alojada en Cloudinary
                .categoria(categoria)
                .tipoProducto(tipoProducto)
                .build();

        productoRepository.save(nuevoProducto);

        return mapearADto(nuevoProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespDTO> obtenerTodosLosProductos() {
        List<Producto> productos = productoRepository.findAll();

        return productos.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
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