package com.software.fixlab.config;

import com.software.fixlab.service.impl.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener la cabecera de autorización de la petición
        final String authHeader = request.getHeader("Authorization");

        // 2. Si no hay cabecera o no empieza con "Bearer ", ignoramos el filtro (ej. rutas públicas como el Login)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extraer el token eliminando los primeros 7 caracteres ("Bearer ")
            final String jwt = authHeader.substring(7);

            // 4. Leer el correo y el rol desde el token usando nuestro servicio
            String correo = jwtService.extraerCorreo(jwt);
            String rol = jwtService.extraerRol(jwt);

            // 5. Si extrajimos el correo exitosamente y el usuario aún no está autenticado en este ciclo
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Le decimos a Spring Security: "Este usuario es válido, déjalo pasar y asígnale su rol"
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        correo,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol))
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si el token fue modificado maliciosamente o ya expiró, el parser lanzará una excepción.
            // La atrapamos aquí silenciosamente. Como no seteamos la autenticación, Spring Security devolverá un 403 Forbidden.
        }

        // 6. Continuar con la cadena de filtros estándar
        filterChain.doFilter(request, response);
    }
}