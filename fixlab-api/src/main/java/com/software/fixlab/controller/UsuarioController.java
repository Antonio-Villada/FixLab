package com.software.fixlab.controller;

import com.software.fixlab.dto.req.EliminarCuentaClienteReqDTO;
import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.ClienteSugerenciaRespDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.StaffTallerAsignableRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioRespDTO>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioRespDTO> obtenerMiPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioRespDTO> actualizarMiPerfil(
            Authentication authentication,
            @RequestBody UsuarioUpdateReqDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarMiPerfil(authentication.getName(), dto));
    }

    @PostMapping("/me/eliminar-cuenta")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<MensajeRespDTO> eliminarMiCuentaCliente(
            Authentication authentication,
            @Valid @RequestBody EliminarCuentaClienteReqDTO dto) {
        return ResponseEntity.ok(usuarioService.eliminarMiCuentaCliente(authentication.getName(), dto));
    }

    @PostMapping(value = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> subirMiFoto(
            Authentication authentication,
            @RequestParam("foto") MultipartFile foto) {
        try {
            return ResponseEntity.ok(usuarioService.subirFotoPerfil(authentication.getName(), foto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO(e.getMessage() != null ? e.getMessage() : "Error al subir la foto"));
        }
    }

    @GetMapping("/sugerencias-clientes")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<List<ClienteSugerenciaRespDTO>> sugerenciasClientes(@RequestParam("q") String q) {
        return ResponseEntity.ok(usuarioService.buscarSugerenciasClientesPorCedula(q));
    }

    @GetMapping("/catalogo/staff-asignable-taller")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<List<StaffTallerAsignableRespDTO>> catalogoStaffAsignableTaller() {
        return ResponseEntity.ok(usuarioService.listarStaffAsignableComoTecnico());
    }

    @GetMapping("/{cedula}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','RECEPCIONISTA')")
    public ResponseEntity<UsuarioRespDTO> obtenerPorCedula(@PathVariable String cedula) {
        return ResponseEntity.ok(usuarioService.obtenerPorCedula(cedula));
    }

    @PutMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN')") // Luego podemos ajustarlo para que el usuario edite su propio perfil
    public ResponseEntity<UsuarioRespDTO> actualizarUsuario(
            @PathVariable String cedula,
            @RequestBody UsuarioUpdateReqDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(cedula, dto));
    }

    @DeleteMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeRespDTO> eliminarUsuario(@PathVariable String cedula) {
        usuarioService.eliminarUsuario(cedula);
        return ResponseEntity.ok(new MensajeRespDTO("Usuario eliminado correctamente"));
    }
}