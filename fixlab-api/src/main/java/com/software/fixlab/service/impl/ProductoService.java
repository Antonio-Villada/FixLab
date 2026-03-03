package com.software.fixlab.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.software.fixlab.entity.Producto;
import com.software.fixlab.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final Cloudinary cloudinary;

    @Transactional
    public Producto crearProducto(String sku, String nombre, String descripcion, Double precio, Integer stock, MultipartFile imagen) throws Exception {

        // 1. Subir la imagen a Cloudinary
        Map respuesta = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.emptyMap());
        String urlImagen = respuesta.get("secure_url").toString();

        // 2. Crear el objeto Producto con la URL obtenida
        Producto nuevoProducto = Producto.builder()
                .sku(sku)
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .stock(stock)
                .activo(true)
                .imagenUrl(urlImagen)
                .build();

        // 3. Guardar en PostgreSQL
        return productoRepository.save(nuevoProducto);
    }

    // Método para el cliente: Solo ve productos disponibles (activos y con stock)
    public List<Producto> listarCatalogoPublico() {
        return productoRepository.findByStockGreaterThanAndActivoTrue(0);
    }

    /** Lista todos los productos (para admin). */
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    /** Actualiza un producto por ID (campos en JSON; no cambia la imagen salvo que se envíe imagenUrl). */
    @Transactional
    public Producto actualizarProducto(Long id, Producto datos) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion() != null ? datos.getDescripcion() : "");
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        if (datos.getImagenUrl() != null) {
            existente.setImagenUrl(datos.getImagenUrl());
        }
        existente.setActivo(datos.getActivo() != null ? datos.getActivo() : true);
        return productoRepository.save(existente);
    }

    /** Soft delete: marca el producto como inactivo. */
    @Transactional
    public void desactivarProducto(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        p.setActivo(false);
        productoRepository.save(p);
    }
}