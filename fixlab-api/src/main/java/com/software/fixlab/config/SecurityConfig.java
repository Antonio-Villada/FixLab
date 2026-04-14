package com.software.fixlab.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Needed so browsers can complete CORS preflight without authentication.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll() // <-- ¡MOVIDO ARRIBA! Antes del anyRequest
                        // Catálogo público (necesario para SSR/hidratación sin que exista localStorage).
                        .requestMatchers(HttpMethod.GET,
                                "/api/productos/**",
                                "/api/categorias/**",
                                "/api/tipos-producto/**").permitAll()
                        // Taller (P3): /api/equipos/** y /api/reparaciones/** requieren JWT (anyRequest authenticated).
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "https://*.ngrok-free.app",
                "https://*.ngrok-free.dev",
                "https://fixlab.villadastudios.com",
                "https://*.villadastudios.com",
                "https://api.villadastudios.com",
                "https://www.fixlabcol.com",
                "https://fixlab.com",
                "https://www.fixlabcol.com:*",
                "http://www.fixlabcol.com",
                "http://fixlab.com",
                "http://www.fixlabcol.com:*",
                "https://app.fixlabcol.com",
                "https://app.fixlabcol.com:*",
                "http://app.fixlabcol.com",
                "http://app.fixlabcol.com:*",
                "http://34.75.187.247",
                "http://34.75.187.247:*",
                "https://34.75.187.247",
                "https://34.75.187.247:*",
                "http://34.171.85.92",
                "http://34.171.85.92:*",
                "https://34.171.85.92",
                "https://34.171.85.92:*"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}