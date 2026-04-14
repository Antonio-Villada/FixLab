package com.software.fixlab.controller;

import com.software.fixlab.dto.req.SolicitudPqrCambiarEstadoReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrCreateReqDTO;
import com.software.fixlab.dto.req.SolicitudPqrValidacionGarantiaReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.SolicitudPqrRespDTO;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.SolicitudPqrService;
import com.software.fixlab.util.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pqrs")
@RequiredArgsConstructor
public class SolicitudPqrController {

    private static final long MAX_EVIDENCIA_BYTES = 25L * 1024 * 1024;

    private final SolicitudPqrService solicitudPqrService;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudPqrRespDTO> crear(
            @Valid @RequestBody SolicitudPqrCreateReqDTO dto,
            Authentication authentication) {
        SolicitudPqrRespDTO creado = solicitudPqrService.crear(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<SolicitudPqrRespDTO>> misSolicitudes(Authentication authentication) {
        return ResponseEntity.ok(solicitudPqrService.listarMis(authentication.getName()));
    }

    @GetMapping("/gestion")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<List<SolicitudPqrRespDTO>> listarGestion(Authentication authentication) {
        return ResponseEntity.ok(solicitudPqrService.listarGestion(
                authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<SolicitudPqrRespDTO> obtenerPorId(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(solicitudPqrService.obtenerPorId(
                id, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudPqrRespDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPqrCambiarEstadoReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(solicitudPqrService.cambiarEstado(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PatchMapping("/{id}/validacion-garantia-fisica")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO')")
    public ResponseEntity<SolicitudPqrRespDTO> validacionGarantiaFisica(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudPqrValidacionGarantiaReqDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(solicitudPqrService.registrarValidacionGarantiaFisica(
                id, dto, authentication.getName(), AuthUtils.rolUsuario(authentication)));
    }

    @PostMapping(value = "/evidencias/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> subirEvidencia(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO("Archivo vacío"));
        }
        if (archivo.getSize() > MAX_EVIDENCIA_BYTES) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO("El archivo supera el tamaño máximo permitido"));
        }
        String ct = archivo.getContentType();
        if (ct == null || !(ct.startsWith("image/") || ct.startsWith("video/"))) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO("Solo se permiten imágenes o videos"));
        }
        try {
            String url = cloudinaryService.subirEvidencia(archivo);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            throw new BadRequestException("No se pudo subir el archivo: " + e.getMessage());
        }
    }
}
