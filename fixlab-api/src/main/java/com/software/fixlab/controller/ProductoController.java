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
    }
}