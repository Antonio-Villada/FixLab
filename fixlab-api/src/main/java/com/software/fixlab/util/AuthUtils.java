package com.software.fixlab.util;

import com.software.fixlab.entity.RolUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static RolUsuario rolUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .map(RolUsuario::valueOf)
                .orElseThrow(() -> new IllegalStateException("No se pudo determinar el rol del usuario"));
    }
}
