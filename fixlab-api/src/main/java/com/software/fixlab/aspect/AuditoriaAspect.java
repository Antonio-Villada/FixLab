package com.software.fixlab.aspect;

import com.software.fixlab.annotation.AuditarAccion;
import com.software.fixlab.entity.RegistroAuditoria;
import com.software.fixlab.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    // @AfterReturning asegura que solo se guarde el registro si el método terminó con éxito (sin errores)
    @AfterReturning(pointcut = "@annotation(auditarAccion)")
    public void registrarAuditoria(JoinPoint joinPoint, AuditarAccion auditarAccion) {
        try {
            // 1. Obtener quién está haciendo la petición
            String usuarioActual = "SISTEMA";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                usuarioActual = auth.getName();
            }

            // 2. Extraer el nombre del método interceptado
            String nombreMetodo = joinPoint.getSignature().getName();
            String detalleLog = "Se ejecutó el método: " + nombreMetodo;

            // 3. Construir el objeto para la base de datos
            RegistroAuditoria registro = RegistroAuditoria.builder()
                    .usuarioEmail(usuarioActual)
                    .modulo(auditarAccion.modulo())
                    .accion(auditarAccion.accion())
                    .detalle(detalleLog)
                    .fechaHora(LocalDateTime.now())
                    .build();

            // 4. Guardar silenciosamente
            auditoriaRepository.save(registro);
            log.info("Auditoría grabada -> Módulo: {} | Acción: {} | Usuario: {}",
                    auditarAccion.modulo(), auditarAccion.accion(), usuarioActual);

        } catch (Exception e) {
            // Un error en la auditoría nunca debe romper la funcionalidad principal
            log.error("Fallo al intentar registrar la auditoría", e);
        }
    }
}