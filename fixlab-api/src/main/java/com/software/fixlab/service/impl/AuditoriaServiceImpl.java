package com.software.fixlab.service.impl;

import com.software.fixlab.entity.RegistroAuditoria;
import com.software.fixlab.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl {

    private final AuditoriaRepository auditoriaRepository;

    public void registrarAccion(String modulo, String accion, String detalle) {
        // 1. Obtener quién está haciendo la petición (El email dentro del Token JWT)
        String usuarioActual = "SISTEMA"; // Por defecto, por si es una acción automática (ej. Webhook Wompi)

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            usuarioActual = authentication.getName();
        }

        // 2. Construir el registro con la hora, minuto y segundo exacto
        RegistroAuditoria registro = RegistroAuditoria.builder()
                .usuarioEmail(usuarioActual)
                .modulo(modulo)
                .accion(accion)
                .detalle(detalle)
                .fechaHora(LocalDateTime.now()) // <--- Aquí captura la estampa de tiempo exacta
                .build();

        // 3. Guardar silenciosamente en la base de datos
        auditoriaRepository.save(registro);
    }
}