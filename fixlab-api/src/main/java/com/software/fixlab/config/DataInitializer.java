package com.software.fixlab.config;

import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String correoAdmin = "admin@fixlab.com";

        // Comprobamos si el administrador ya existe para no duplicarlo al reiniciar el servidor
        if (!usuarioRepository.existsByEmail(correoAdmin)) {

            Usuario admin = Usuario.builder()
                    .cedula("00000000")
                    .nombre("Administrador")
                    .apellidos("Principal")
                    .direccion("Sistema")
                    .email(correoAdmin)
                    .password(passwordEncoder.encode("Admin123456"))
                    .telefono("3001112233")
                    .rol(RolUsuario.ADMIN)
                    .intentosFallidos(0)
                    .build();

            usuarioRepository.save(admin);

            // Un pequeño mensaje en la consola de Ubuntu para avisarte que funcionó
            System.out.println("=========================================================");
            System.out.println("✅ USUARIO ADMINISTRADOR CREADO AUTOMÁTICAMENTE");
            System.out.println("📧 Correo: " + correoAdmin);
            System.out.println("🔑 Clave: Admin123456");
            System.out.println("=========================================================");
        }
    }
}