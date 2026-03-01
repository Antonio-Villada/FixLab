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

    // Método para el cliente: Solo ve productos disponibles
    public List<Producto> listarCatalogoPublico() {
        return productoRepository.findByStockGreaterThanAndActivoTrue(0);
    }
}