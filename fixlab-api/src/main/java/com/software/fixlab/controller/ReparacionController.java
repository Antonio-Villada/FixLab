package com.software.fixlab.controller;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.*;
import com.software.fixlab.service.interfaces.ReparacionService;
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
@RequestMapping("/api/reparaciones")
@RequiredArgsConstructor
public class ReparacionController {

    private final ReparacionService reparacionService;

    @GetMapping("/catalogo/tipos-equipo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoEquipoRespDTO>> listarTiposEquipo() {
        return ResponseEntity.ok(reparacionService.listarTiposEquipo());
    }

    @GetMapping("/catalogo/tipos-taller")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoTallerRespDTO>> listarTiposTaller() {
        return ResponseEntity.ok(reparacionService.listarTiposTaller());
    }

    @GetMapping("/catalogo/talleres")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TallerRespDTO>> listarTalleres() {
        return ResponseEntity.ok(reparacionService.listarTalleres());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<ReparacionRespDTO> crear(
            @Valid @RequestBody ReparacionCreateReqDTO dto,
            Authentication authentication) {
        ReparacionRespDTO creado = reparacionService.crear(
                dto, authentication.getName(), AuthUtils.rolUsuario(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<List<ReparacionRespDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(reparacionService.listar(authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @GetMapping("/por-ticket/{numero}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO')")
    public ResponseEntity<ReparacionRespDTO> obtenerPorNumeroTicket(
            @PathVariable String numero,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.obtenerPorNumeroTicket(
                numero, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<ReparacionRespDTO> obtenerPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.obtenerPorId(
                id, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PatchMapping("/{id}/asignar-tecnico")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<ReparacionRespDTO> asignarTecnico(
            @PathVariable Integer id,
            @Valid @RequestBody ReparacionAsignarTecnicoReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.asignarTecnico(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PatchMapping("/{id}/diagnostico")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ReparacionRespDTO> registrarDiagnostico(
            @PathVariable Integer id,
            @Valid @RequestBody ReparacionDiagnosticoCotizacionReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.registrarDiagnosticoCotizacion(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ReparacionRespDTO> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody ReparacionCambiarEstadoReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.cambiarEstado(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PostMapping("/{id}/productos")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<ReparacionRespDTO> agregarProducto(
            @PathVariable Integer id,
            @Valid @RequestBody ReparacionProductoReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.agregarProducto(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PostMapping("/{id}/evidencias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReparacionRespDTO> agregarEvidencia(
            @PathVariable Integer id,
            @Valid @RequestBody ReparacionEvidenciaReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reparacionService.agregarEvidencia(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PostMapping("/{id}/aprobar-cotizacion")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReparacionRespDTO> aprobarCotizacion(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(reparacionService.aprobarCotizacion(
                id, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }
}
