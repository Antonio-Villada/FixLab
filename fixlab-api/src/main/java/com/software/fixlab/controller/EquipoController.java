package com.software.fixlab.controller;

import com.software.fixlab.dto.req.EquipoReqDTO;
import com.software.fixlab.dto.resp.EquipoRespDTO;
import com.software.fixlab.service.interfaces.EquipoService;
import com.software.fixlab.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<EquipoRespDTO> crear(
            @Valid @RequestBody EquipoReqDTO dto,
            Authentication authentication) {
        EquipoRespDTO creado = equipoService.crear(dto, authentication.getName(), AuthUtils.rolUsuario(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<List<EquipoRespDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(equipoService.listar(authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<EquipoRespDTO> obtenerPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(equipoService.obtenerPorId(id, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }
}
