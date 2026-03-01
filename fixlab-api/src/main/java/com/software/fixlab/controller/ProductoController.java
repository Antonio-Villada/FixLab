package com.software.fixlab.controller;

import com.software.fixlab.entity.Producto;
import com.software.fixlab.service.impl.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // Ruta pública: Ver catálogo (clientes y visitantes)
    @GetMapping
    public ResponseEntity<List<Producto>> verCatalogo() {
        return ResponseEntity.ok(productoService.listarCatalogoPublico());
    }

    // Ruta protegida: Crear producto (Solo ADMIN)
    // Usamos consumes = MULTIPART_FORM_DATA_VALUE porque recibiremos un archivo físico
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> crearProducto(
            @RequestParam("sku") String sku,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") Double precio,
            @RequestParam("stock") Integer stock,
            @RequestParam("imagen") MultipartFile imagen) throws Exception {

        Producto productoCreado = productoService.crearProducto(sku, nombre, descripcion, precio, stock, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
    }
}