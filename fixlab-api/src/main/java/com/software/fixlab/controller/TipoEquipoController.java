package com.software.fixlab.controller;

import com.software.fixlab.dto.req.TipoEquipoReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TipoEquipoRespDTO;
import com.software.fixlab.service.interfaces.TipoEquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-equipo")
@RequiredArgsConstructor
public class TipoEquipoController {

    private final TipoEquipoService tipoEquipoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoEquipoRespDTO> crear(@Valid @RequestBody TipoEquipoReqDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoEquipoService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoEquipoRespDTO>> listar() {
        return ResponseEntity.ok(tipoEquipoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TipoEquipoRespDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoEquipoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TipoEquipoRespDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody TipoEquipoReqDTO dto) {
        return ResponseEntity.ok(tipoEquipoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeRespDTO> eliminar(@PathVariable Integer id) {
        tipoEquipoService.eliminar(id);
        return ResponseEntity.ok(new MensajeRespDTO("Tipo de equipo eliminado correctamente."));
    }
}
