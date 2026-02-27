package com.software.fixlab.controller;

import com.software.fixlab.dto.req.LoginReqDTO;
import com.software.fixlab.dto.req.RegistroReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<TokenRespDTO> login(@Valid @RequestBody LoginReqDTO loginReqDTO) throws Exception {
        TokenRespDTO respuesta = authService.login(loginReqDTO);
        // Retornamos 200 OK con el token
        return ResponseEntity.ok(respuesta);
    }
}