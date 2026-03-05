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
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.ProductoService;
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