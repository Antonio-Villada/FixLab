package com.software.fixlab.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.software.fixlab.dto.req.EntradaMercanciaReqDTO;
import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import com.software.fixlab.dto.resp.EntradaMercanciaRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.entity.Categoria;
import com.software.fixlab.entity.EntradaMercancia;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteCategoriaException;
import com.software.fixlab.exception.NoExisteProductoException;
import com.software.fixlab.exception.NoExisteTipoProductoException;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.repository.EntradaMercanciaRepository;
import com.software.fixlab.repository.ProductoRepository;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.ProductoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final EntradaMercanciaRepository entradaMercanciaRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ProductoRespDTO crearProducto(ProductoReqDTO dto, MultipartFile imagen) {
        // Validación de relaciones
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new NoExisteCategoriaException("La categoría con ID " + dto.getCategoriaId() + " no existe."));

        TipoProducto tipoProducto = tipoProductoRepository.findById(dto.getTipoProductoId())
                .orElseThrow(() -> new NoExisteTipoProductoException("El tipo de producto con ID " + dto.getTipoProductoId() + " no existe."));

        int stockMinimo = resolverStockMinimo(dto);

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
                .stockMinimo(stockMinimo)
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
        producto.setStockMinimo(resolverStockMinimo(dto));
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

    @Override
    @Transactional
    public EntradaMercanciaRespDTO registrarEntradaMercancia(Long productoId, EntradaMercanciaReqDTO dto) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NoExisteProductoException("Producto no encontrado con ID: " + productoId));

        EntradaMercancia entrada = EntradaMercancia.builder()
                .producto(producto)
                .cantidad(dto.getCantidad())
                .comentario(dto.getComentario() != null && !dto.getComentario().isBlank()
                        ? dto.getComentario().trim()
                        : null)
                .fechaRegistro(Instant.now())
                .build();
        entradaMercanciaRepository.save(entrada);

        int nuevoStock = producto.getStock() + dto.getCantidad();
        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        return EntradaMercanciaRespDTO.builder()
                .id(entrada.getId())
                .productoId(producto.getId())
                .sku(producto.getSku())
                .nombreProducto(producto.getNombre())
                .cantidad(dto.getCantidad())
                .nuevoStock(nuevoStock)
                .comentario(entrada.getComentario())
                .fechaRegistro(entrada.getFechaRegistro())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRespDTO> listarProductosConStockBajo() {
        return productoRepository.findActivosConStockBajo().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaMercanciaRespDTO> listarEntradasMercancia(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new NoExisteProductoException("Producto no encontrado con ID: " + productoId);
        }
        return entradaMercanciaRepository.findByProducto_IdOrderByFechaRegistroDesc(productoId).stream()
                .map(this::mapearEntradaADto)
                .collect(Collectors.toList());
    }

    private int resolverStockMinimo(ProductoReqDTO dto) {
        int v = dto.getStockMinimo() != null ? dto.getStockMinimo() : 5;
        if (v < 0) {
            throw new BadRequestException("El stock mínimo no puede ser negativo.");
        }
        return v;
    }

    private ProductoRespDTO mapearADto(Producto producto) {
        int stockMinimoResp = producto.getStockMinimo() != null ? producto.getStockMinimo() : 5;
        return ProductoRespDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .stockMinimo(stockMinimoResp)
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

    private EntradaMercanciaRespDTO mapearEntradaADto(EntradaMercancia e) {
        Producto p = e.getProducto();
        return EntradaMercanciaRespDTO.builder()
                .id(e.getId())
                .productoId(p.getId())
                .sku(p.getSku())
                .nombreProducto(p.getNombre())
                .cantidad(e.getCantidad())
                .comentario(e.getComentario())
                .fechaRegistro(e.getFechaRegistro())
                .build();
    }
}