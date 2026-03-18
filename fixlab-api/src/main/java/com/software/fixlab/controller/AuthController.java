package com.software.fixlab.controller;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.CheckDisposableResp;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.service.interfaces.AuthService;
import com.software.fixlab.util.DisposableEmailValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Consulta pública para que el frontend valide si un correo es temporal (lista cargada desde GitHub). */
    @GetMapping("/check-disposable")
    public ResponseEntity<CheckDisposableResp> checkDisposable(@RequestParam String email) {
        boolean disposable = DisposableEmailValidator.isDisposable(email);
        return ResponseEntity.ok(new CheckDisposableResp(disposable));
    }

    @PostMapping("/registro")
    public ResponseEntity<MensajeRespDTO> registrarCliente(@Valid @RequestBody RegistroReqDTO registroReqDTO) throws Exception {
        MensajeRespDTO respuesta = authService.registrarCliente(registroReqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping(value = "/registro-con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MensajeRespDTO> registrarClienteConFoto(
            @RequestParam String cedula,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String telefono,
            @RequestParam(required = false) MultipartFile foto) throws Exception {
        RegistroReqDTO dto = new RegistroReqDTO();
        dto.setCedula(cedula);
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setTelefono(telefono);
        MensajeRespDTO respuesta = authService.registrarClienteConFoto(dto, foto);
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

    @PostMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cambiarPassword(Authentication authentication, @RequestBody CambiarPasswordReqDTO dto) {
        try {
            authService.cambiarPassword(authentication.getName(), dto.getContraseñaActual(), dto.getNuevaPassword());
            return ResponseEntity.ok(new MensajeRespDTO("Contraseña actualizada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO(e.getMessage()));
        }
    }

    @PostMapping("/admin/asignar-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> asignarNuevaPassword(@Valid @RequestBody AdminAsignarPasswordReqDTO dto) {
        try {
            authService.asignarNuevaPasswordPorCedula(dto.getCedula(), dto.getNuevaPassword());
            return ResponseEntity.ok(new MensajeRespDTO("Contraseña asignada correctamente. El usuario ya puede iniciar sesión con la nueva contraseña."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeRespDTO(e.getMessage()));
        }
    }
}