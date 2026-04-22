package com.software.fixlab.controller;

import com.software.fixlab.dto.req.EntradaMercanciaReqDTO;
import com.software.fixlab.dto.resp.EntradaMercanciaRespDTO;
import com.software.fixlab.dto.resp.ProductoRespDTO;
import com.software.fixlab.service.interfaces.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductoStockAdminController {

    private final ProductoService productoService;

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<ProductoRespDTO>> listarStockBajo() {
        return ResponseEntity.ok(productoService.listarProductosConStockBajo());
    }

    @PostMapping("/{id}/entrada-mercancia")
    public ResponseEntity<EntradaMercanciaRespDTO> registrarEntrada(
            @PathVariable Long id,
            @Valid @RequestBody EntradaMercanciaReqDTO dto) {
        return ResponseEntity.ok(productoService.registrarEntradaMercancia(id, dto));
    }

    @GetMapping("/{id}/entradas-mercancia")
    public ResponseEntity<List<EntradaMercanciaRespDTO>> listarEntradas(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.listarEntradasMercancia(id));
    }
}
