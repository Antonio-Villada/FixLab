package com.software.fixlab.config;

import com.software.fixlab.entity.Categoria;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String[] CATEGORIAS_INICIALES = { "Repuestos", "Productos" };
    private static final String[] TIPOS_PRODUCTO_INICIALES = { "RAM", "Impresoras", "Parlantes", "UPS o reguladores" };

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoProductoRepository tipoProductoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        inicializarAdmin();
        inicializarCategorias();
        inicializarTiposProducto();
    }

    private void inicializarAdmin() {
        String correoAdmin = "admin@fixlab.com";
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

    private void inicializarCategorias() {
        for (String nombre : CATEGORIAS_INICIALES) {
            if (categoriaRepository.findByNombre(nombre).isEmpty()) {
                categoriaRepository.save(Categoria.builder().nombre(nombre).build());
            }
        }
    }

    private void inicializarTiposProducto() {
        for (String nombre : TIPOS_PRODUCTO_INICIALES) {
            if (tipoProductoRepository.findByNombre(nombre).isEmpty()) {
                tipoProductoRepository.save(TipoProducto.builder().nombre(nombre).build());
            }
        }
    }
}