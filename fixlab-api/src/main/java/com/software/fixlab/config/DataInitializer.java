package com.software.fixlab.config;

import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Taller;
import com.software.fixlab.entity.TipoTaller;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.TallerRepository;
import com.software.fixlab.repository.TipoTallerRepository;
import com.software.fixlab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TipoTallerRepository tipoTallerRepository;
    private final TallerRepository tallerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        inicializarAdmin();
        inicializarCatalogoTaller();
    }

    private void inicializarCatalogoTaller() {
       
        if (tallerRepository.count() > 0) {
            return;
        }
        TipoTaller tipoTaller = tipoTallerRepository.findAll().stream().findFirst()
                .orElseGet(() -> tipoTallerRepository.save(TipoTaller.builder()
                        .nombre("General")
                        .ciclo("OPERATIVO")
                        .estado("ACTIVO")
                        .build()));
        tallerRepository.save(Taller.builder()
                .nombre("Fixlab - Taller principal")
                .tipoTaller(tipoTaller)
                .build());
    }

    private void inicializarAdmin() {
        String correoAdmin = "ivanramiro654@gmail.com";
        if (usuarioRepository.findByEmail(correoAdmin).isEmpty()) {
            Usuario admin = Usuario.builder()
                    .cedula("0000000000")
                    .nombre("Administrador")
                    .apellido("Principal")
                    .email(correoAdmin)
                    .password(passwordEncoder.encode("Admin123456"))
                    .telefono("3001112233")
                    .rol(RolUsuario.ADMIN)
                    .intentosFallidos(0)
                    .correoVerificado(true)
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