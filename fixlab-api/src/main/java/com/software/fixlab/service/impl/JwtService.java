package com.software.fixlab.service.impl;

import com.software.fixlab.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // Inyectamos la clave que pusimos en application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    public String generarToken(Usuario usuario) {
        // En el "Payload" (cuerpo del token) podemos guardar datos no sensibles útiles para el frontend
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("nombre", usuario.getNombre());

        return Jwts.builder()
                .setClaims(extraClaims) // Agregamos los datos extra
                .setSubject(usuario.getEmail()) // El "subject" principal es el correo
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Expira en 24 horas
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Firmamos con nuestra clave secreta
                .compact();
    }

    // Método auxiliar para decodificar la clave secreta
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Método para extraer el correo (subject) del token
    public String extraerCorreo(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Método para extraer el rol del token
    public String extraerRol(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rol", String.class);
    }
}