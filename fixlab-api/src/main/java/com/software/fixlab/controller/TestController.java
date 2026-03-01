package com.software.fixlab.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // Ruta protegida: Solo accesible si el token tiene el rol CLIENTE
    @GetMapping("/cliente")
    @PreAuthorize("hasRole('CLIENTE')")
    public String accesoCliente() {
        return "¡Éxito! El filtro JWT te dejó pasar. Tienes acceso al panel de CLIENTE.";
    }

    // Ruta protegida: Solo accesible si el token tiene el rol ADMIN
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String accesoAdmin() {
        return "¡Éxito! Tienes acceso al panel de ADMINISTRADOR.";
    }
}