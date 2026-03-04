package com.software.fixlab.controller;

import com.software.fixlab.dto.req.TipoProductoReqDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.service.interfaces.TipoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-producto")
@RequiredArgsConstructor
public class TipoProductoController {

    private final TipoProductoService tipoProductoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearTipoProducto(@RequestBody TipoProductoReqDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(tipoProductoService.crearTipoProducto(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<TipoProductoRespDTO>> obtenerTodos() {
        return ResponseEntity.ok(tipoProductoService.obtenerTodos());
    }
}