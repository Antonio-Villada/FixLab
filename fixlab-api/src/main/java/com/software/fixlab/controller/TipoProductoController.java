package com.software.fixlab.controller;

import com.software.fixlab.dto.req.TipoProductoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteTipoProductoException;
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
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<TipoProductoRespDTO>> obtenerTodos() {
        return ResponseEntity.ok(tipoProductoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(tipoProductoService.obtenerPorId(id));
        } catch (NoExisteTipoProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarTipoProducto(@PathVariable Integer id, @RequestBody TipoProductoReqDTO dto) {
        try {
            return ResponseEntity.ok(tipoProductoService.actualizarTipoProducto(id, dto));
        } catch (NoExisteTipoProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarTipoProducto(@PathVariable Integer id) {
        try {
            tipoProductoService.eliminarTipoProducto(id);
            return ResponseEntity.ok(new MensajeRespDTO("Tipo de producto eliminado exitosamente."));
        } catch (NoExisteTipoProductoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeRespDTO(e.getMessage()));
        }
    }
}