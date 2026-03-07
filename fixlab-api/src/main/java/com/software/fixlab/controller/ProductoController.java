package com.software.fixlab.controller;

import com.software.fixlab.dto.req.ProductoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteCategoriaException;
import com.software.fixlab.exception.NoExisteProductoException;
import com.software.fixlab.exception.NoExisteTipoProductoException;
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

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearProducto(
            @ModelAttribute ProductoReqDTO dto,
            @RequestParam("imagen") MultipartFile imagen) {
        try {
            ProductoRespDTO productoCreado = productoService.crearProducto(dto, imagen);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);
        } catch (NoExisteCategoriaException | NoExisteTipoProductoException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MensajeRespDTO("Error interno del servidor al procesar la solicitud."));
        }
    }

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

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute ProductoReqDTO dto,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {
        try {
            return ResponseEntity.ok(productoService.actualizarProducto(id, dto, imagen));
        } catch (NoExisteProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        } catch (NoExisteCategoriaException | NoExisteTipoProductoException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MensajeRespDTO("Error interno del servidor al procesar la solicitud."));
        }
    }

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