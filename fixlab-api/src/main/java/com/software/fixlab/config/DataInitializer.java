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

        // Usamos findByEmail().isEmpty() ya que es el método que tenemos en el repositorio
        if (usuarioRepository.findByEmail(correoAdmin).isEmpty()) {

            Usuario admin = Usuario.builder()
                    .cedula("0000000000") // <- Nueva llave primaria
                    .nombre("Administrador") // <- Separado
                    .apellido("Principal") // <- Separado
                    .email(correoAdmin)
                    .password(passwordEncoder.encode("Admin123456"))
                    .telefono("3001112233")
                    .rol(RolUsuario.ADMIN)
                    .intentosFallidos(0)
                    .correoVerificado(true) // <- Nace verificado para que no lo bloquee el sistema
                    .build();

            usuarioRepository.save(admin);

            System.out.println("=========================================================");
            System.out.println("✅ USUARIO ADMINISTRADOR CREADO AUTOMÁTICAMENTE");
            System.out.println("🆔 Cédula: 0000000000");
            System.out.println("📧 Correo: " + correoAdmin);
            System.out.println("🔑 Clave: Admin123456");
            System.out.println("=========================================================");
        }
    }
}