package com.software.fixlab.controller;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

<<<<<<< HEAD
    // Cambiamos el consumes para aceptar archivos físicos (form-data)
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearProducto(
            @ModelAttribute ProductoReqDTO dto, // <-- Recibe los campos de texto sueltos
            @RequestParam("imagen") MultipartFile imagen) { // <-- Recibe el archivo físico
        try {
            ProductoRespDTO productoCreado = productoService.crearProducto(dto, imagen);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new com.software.fixlab.dto.resp.MensajeRespDTO(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductoRespDTO>> obtenerProductos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
=======
    /** Lista todos los productos. El frontend filtra por activo para el catálogo público. */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    /** Crear producto (solo ADMIN). Recibe multipart: sku, nombre, descripcion, precio, stock, imagen. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
>>>>>>> ebc21313413cb4bf66ee58a55f8bed5b9e914097
    }

    /** Actualizar producto (solo ADMIN). Cuerpo JSON: nombre, descripcion, precio, stock, imagenUrl, activo. */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto datos) {
        Producto actualizado = productoService.actualizarProducto(id, datos);
        return ResponseEntity.ok(actualizado);
    }

    /** Soft delete: desactiva el producto (solo ADMIN). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }
}