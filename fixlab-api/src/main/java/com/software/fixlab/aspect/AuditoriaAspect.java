package com.software.fixlab.aspect;

import com.software.fixlab.entity.RegistroAuditoria;
import com.software.fixlab.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    // 🎯 EL RADAR: Intercepta TODOS los métodos de CUALQUIER clase dentro del paquete 'controller'
    @Pointcut("execution(* com.software.fixlab.controller..*(..))")
    public void todosLosControladores() {}

    // Se ejecuta automáticamente justo después de que cualquier controlador termine su trabajo
    @AfterReturning(pointcut = "todosLosControladores()")
    public void registrarTodo(JoinPoint joinPoint) {
        try {
            // 1. ¿QUIÉN lo hizo? (Extraemos el email del token JWT)
            String usuarioActual = "SISTEMA / VISITANTE NO LOGUEADO";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                usuarioActual = auth.getName();
            }

            // 2. ¿DÓNDE lo hizo? (Extraemos la ruta HTTP exacta como /api/productos)
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ruta = "N/A";
            String metodoHttp = "N/A";
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ruta = request.getRequestURI();
                metodoHttp = request.getMethod();
            }

            // ⚠️ OPCIONAL: Si no quieres que se llene la base de datos cada vez que alguien solo "mira" productos
            // descomenta las siguientes 3 líneas para ignorar las peticiones GET.
            if ("GET".equalsIgnoreCase(metodoHttp)) {
               return;
            }

            // 3. ¿QUÉ hizo? (Calculamos el Módulo y la Acción automáticamente)
            // Ejemplo: ProductoController -> se convierte en Módulo "Producto"
            String nombreClase = joinPoint.getTarget().getClass().getSimpleName();
            String moduloDeducido = nombreClase.replace("Controller", "").toUpperCase();

            // Ejemplo: Método HTTP POST + nombre del método Java -> "POST crearProducto"
            String nombreMetodoJava = joinPoint.getSignature().getName();
            String accionDeducida = metodoHttp + " - " + nombreMetodoJava;

            // Tratamos de capturar qué datos envió (con cuidado de no colapsar la memoria)
            String detalleLog = "Ruta: " + ruta;
            try {
                detalleLog += " | Datos enviados: " + Arrays.toString(joinPoint.getArgs());
                // Limitamos a 500 caracteres por si envían una imagen muy pesada en Base64
                if (detalleLog.length() > 500) {
                    detalleLog = detalleLog.substring(0, 500) + "... [texto truncado]";
                }
            } catch (Exception e) {
                detalleLog += " | Datos enviados: [No se pudieron leer]";
            }

            // 4. Construimos y guardamos el registro en la BD
            RegistroAuditoria registro = RegistroAuditoria.builder()
                    .usuarioEmail(usuarioActual)
                    .modulo(moduloDeducido)
                    .accion(accionDeducida)
                    .detalle(detalleLog)
                    .fechaHora(LocalDateTime.now()) // Hora, minuto y segundo exacto del sistema
                    .build();

            auditoriaRepository.save(registro);

        } catch (Exception e) {
            log.error("Error silencioso guardando auditoría global", e);
        }
    }
}