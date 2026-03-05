package com.software.fixlab.controller;

import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
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