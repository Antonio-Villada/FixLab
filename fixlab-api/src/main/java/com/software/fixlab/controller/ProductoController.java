package com.software.fixlab.controller;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.exception.*;
import com.software.fixlab.service.interfaces.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // --- CREACIÓN (ADMIN) ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearProducto(@ModelAttribute ProductoReqDTO dto) {
        try {
            // Nota: El 'imagen' viene dentro del DTO como MultipartFile
            ProductoRespDTO productoCreado = productoService.crearProducto(dto, dto.getImagen());
            return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
        } catch (NoExisteCategoriaException | NoExisteTipoProductoException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeRespDTO("Error al subir producto: " + e.getMessage()));
        }
    }

    // --- CONSULTA (PÚBLICO) ---
    @GetMapping
    public ResponseEntity<List<ProductoRespDTO>> obtenerProductos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.obtenerPorId(id));
        } catch (NoExisteProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    // --- ACTUALIZACIÓN (ADMIN) ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @ModelAttribute ProductoReqDTO dto) {
        try {
            return ResponseEntity.ok(productoService.actualizarProducto(id, dto, dto.getImagen()));
        } catch (NoExisteProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        } catch (NoExisteCategoriaException | NoExisteTipoProductoException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MensajeRespDTO("Error al actualizar: " + e.getMessage()));
        }
    }

    // --- ELIMINACIÓN/DESACTIVACIÓN (ADMIN) ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok(new MensajeRespDTO("Producto desactivado exitosamente."));
        } catch (NoExisteProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }
}