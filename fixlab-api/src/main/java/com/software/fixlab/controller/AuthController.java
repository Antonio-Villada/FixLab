package com.software.fixlab.controller;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<MensajeRespDTO> registrarCliente(@Valid @RequestBody RegistroReqDTO registroReqDTO) throws Exception {
        MensajeRespDTO respuesta = authService.registrarCliente(registroReqDTO);
        // Retornamos 201 Created cuando se crea un recurso exitosamente
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/cambiar-rol")
    @PreAuthorize("hasRole('ADMIN')") // <-- OTRA VEZ EL ESCUDO DE SEGURIDAD
    public ResponseEntity<MensajeRespDTO> cambiarRol(@RequestBody CambioRolReqDTO dto) {
        try {
            MensajeRespDTO respuesta = authService.cambiarRol(dto);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PostMapping("/verificar-correo")
    public ResponseEntity<MensajeRespDTO> verificarCorreo(@RequestBody VerificarCorreoReqDTO dto) {
        try {
            return ResponseEntity.ok(authService.verificarCorreo(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<TokenRespDTO> login(@Valid @RequestBody LoginReqDTO loginReqDTO) throws Exception {
        TokenRespDTO respuesta = authService.login(loginReqDTO);
        // Retornamos 200 OK con el token
        return ResponseEntity.ok(respuesta);
    }
    @PostMapping("/registro-empleado")
    @PreAuthorize("hasRole('ADMIN')") // <-- ESTE ES EL ESCUDO DE SEGURIDAD
    public ResponseEntity<MensajeRespDTO> registrarEmpleado(@RequestBody RegistroEmpleadoReqDTO dto) {
        try {
            MensajeRespDTO respuesta = authService.registrarEmpleado(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody SolicitarRecuperacionDTO dto) {
        try {
            authService.solicitarRecuperacionPassword(dto.getEmail());
            // Siempre respondemos OK por seguridad, incluso si el correo no existe, para evitar filtración de datos (Enumeration Attack)
            return ResponseEntity.ok(new MensajeRespDTO("Si el correo existe, se han enviado las instrucciones."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetearPassword(@RequestBody ResetearPasswordDTO dto) {
        try {
            authService.cambiarPasswordConToken(dto.getToken(), dto.getNuevaPassword());
            return ResponseEntity.ok(new MensajeRespDTO("Contraseña actualizada correctamente. Ya puedes iniciar sesión."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO(e.getMessage()));
        }
    }
}