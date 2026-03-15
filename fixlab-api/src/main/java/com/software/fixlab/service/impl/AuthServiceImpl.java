package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.mapper.UsuarioMapper;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.AuthService;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.util.DisposableEmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Contraseña: mínimo 8 caracteres, al menos una letra, un número y un carácter especial. */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$");
    private static final String PASSWORD_FORMATO_MSG = "La contraseña debe tener al menos 8 caracteres, incluyendo letras, números y caracteres especiales.";

    private static void validarFormatoPassword(String password) throws Exception {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new Exception(PASSWORD_FORMATO_MSG);
        }
    }

    /** URL base del frontend (ej. https://tuapp.com). En local puede ser http://localhost:4200 */
    @Value("${fixlab.frontend.url:http://localhost:4200}")
    private String frontendBaseUrl;

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public MensajeRespDTO registrarCliente(RegistroReqDTO dto) throws Exception {

        if (DisposableEmailValidator.isDisposable(dto.getEmail())) {
            throw new Exception("No se permite el registro con correos temporales o desechables. Por favor utiliza un correo electrónico permanente.");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("El correo electrónico ya se encuentra registrado.");
        }

        if (usuarioRepository.existsById(dto.getCedula())) {
            throw new Exception("La cédula ya se encuentra registrada en el sistema.");
        }
        validarFormatoPassword(dto.getPassword());

        // Generamos un código aleatorio de 6 dígitos
        String codigoGenerado = String.format("%06d", new java.util.Random().nextInt(999999));

        // CONSTRUIMOS EL USUARIO MANUALMENTE PARA EVITAR EL ERROR DEL MAPPER
        Usuario nuevoUsuario = Usuario.builder()
                .cedula(dto.getCedula())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(RolUsuario.CLIENTE)
                .intentosFallidos(0)
                .correoVerificado(false)
                .codigoVerificacion(codigoGenerado)
                .expiracionCodigo(LocalDateTime.now().plusMinutes(15))
                .build();

        // Ahora sí, guardamos el usuario con la cédula garantizada
        usuarioRepository.save(nuevoUsuario);

        // Enviamos el correo real
        emailService.enviarCodigoVerificacion(nuevoUsuario.getEmail(), nuevoUsuario.getNombre(), codigoGenerado);

        return new MensajeRespDTO("Registro exitoso. Hemos enviado un código de 6 dígitos a tu correo para verificar tu cuenta.");
    }

    @Override
    @Transactional
    public MensajeRespDTO registrarClienteConFoto(RegistroReqDTO dto, MultipartFile foto) throws Exception {
        if (DisposableEmailValidator.isDisposable(dto.getEmail())) {
            throw new Exception("No se permite el registro con correos temporales o desechables. Por favor utiliza un correo electrónico permanente.");
        }
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("El correo electrónico ya se encuentra registrado.");
        }
        if (usuarioRepository.existsById(dto.getCedula())) {
            throw new Exception("La cédula ya se encuentra registrada en el sistema.");
        }
        validarFormatoPassword(dto.getPassword());
        String codigoGenerado = String.format("%06d", new java.util.Random().nextInt(999999));
        Usuario nuevoUsuario = Usuario.builder()
                .cedula(dto.getCedula())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(RolUsuario.CLIENTE)
                .intentosFallidos(0)
                .correoVerificado(false)
                .codigoVerificacion(codigoGenerado)
                .expiracionCodigo(LocalDateTime.now().plusMinutes(15))
                .build();
        if (foto != null && !foto.isEmpty()) {
            try {
                String urlFoto = cloudinaryService.subirImagen(foto);
                nuevoUsuario.setFotoUrl(urlFoto);
            } catch (Exception e) {
                log.warn("Error al subir foto de perfil en registro: {}", e.getMessage());
            }
        }
        usuarioRepository.save(nuevoUsuario);
        emailService.enviarCodigoVerificacion(nuevoUsuario.getEmail(), nuevoUsuario.getNombre(), codigoGenerado);
        return new MensajeRespDTO("Registro exitoso. Hemos enviado un código de 6 dígitos a tu correo para verificar tu cuenta.");
    }

    @Override
    @Transactional
    public MensajeRespDTO registrarEmpleado(RegistroEmpleadoReqDTO dto) throws Exception {

        if (DisposableEmailValidator.isDisposable(dto.getEmail())) {
            throw new Exception("No se permite el registro con correos temporales o desechables. Por favor utiliza un correo electrónico permanente.");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("El correo electrónico ya se encuentra registrado.");
        }

        if (usuarioRepository.existsById(dto.getCedula())) {
            throw new Exception("La cédula ya se encuentra registrada en el sistema.");
        }
        validarFormatoPassword(dto.getPassword());

        Usuario nuevoEmpleado = Usuario.builder()
                .cedula(dto.getCedula())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .rol(dto.getRol()) // <-- Aquí le asignamos el rol que eligió el Administrador
                .intentosFallidos(0)
                .correoVerificado(true) // El técnico nace con la cuenta activa
                .build();

        usuarioRepository.save(nuevoEmpleado);

        return new MensajeRespDTO("Empleado registrado exitosamente como " + dto.getRol());
    }

    @Override
    @Transactional
    public TokenRespDTO login(LoginReqDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new Exception("Credenciales incorrectas"));

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new Exception("Cuenta bloqueada por múltiples intentos. Intente de nuevo más tarde.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            manejarIntentoFallido(usuario);
            throw new Exception("Credenciales incorrectas");
        }

        // Clientes deben tener el correo verificado para poder iniciar sesión
        if (usuario.getRol() == RolUsuario.CLIENTE && !usuario.isCorreoVerificado()) {
            throw new Exception("Debes verificar tu correo antes de iniciar sesión. Revisa el código de 6 dígitos que enviamos a tu email.");
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        // Generamos el token real
        String jwtToken = jwtService.generarToken(usuario);
        return new TokenRespDTO(jwtToken, usuario.getRol().name());
    }

    @Override
    @Transactional
    public MensajeRespDTO cambiarRol(CambioRolReqDTO dto) throws Exception {
        // 1. Buscamos al usuario por su llave primaria (cédula)
        Usuario usuario = usuarioRepository.findById(dto.getCedula())
                .orElseThrow(() -> new Exception("No se encontró ningún usuario con la cédula: " + dto.getCedula()));

        // 2. Opcional pero recomendado: Evitar que el admin se quite el rol a sí mismo por accidente
        if (usuario.getRol() == RolUsuario.ADMIN && dto.getNuevoRol() != RolUsuario.ADMIN) {
            // Aquí podrías validar si es el único admin del sistema, pero por ahora lo dejamos actualizar
        }

        // 3. Actualizamos el rol y guardamos
        usuario.setRol(dto.getNuevoRol());
        usuarioRepository.save(usuario);

        return new MensajeRespDTO("El rol del usuario " + usuario.getNombre() + " ha sido actualizado a " + dto.getNuevoRol());
    }

    private void manejarIntentoFallido(Usuario usuario) {
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

        if (usuario.getIntentosFallidos() >= 3) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
            usuario.setIntentosFallidos(0);
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    @Override
    public MensajeRespDTO verificarCorreo(VerificarCorreoReqDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        if (usuario.isCorreoVerificado()) {
            return new MensajeRespDTO("La cuenta ya se encuentra verificada.");
        }
        if (usuario.getExpiracionCodigo().isBefore(LocalDateTime.now())) {
            throw new Exception("El código ha expirado. Por favor solicita uno nuevo.");
        }
        if (!usuario.getCodigoVerificacion().equals(dto.getCodigo())) {
            throw new Exception("El código de verificación es incorrecto.");
        }

        // Si todo está bien, verificamos la cuenta y limpiamos el código
        usuario.setCorreoVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuario.setExpiracionCodigo(null);
        usuarioRepository.save(usuario);

        return new MensajeRespDTO("¡Cuenta verificada con éxito! Ya puedes iniciar sesión.");
    }

    @Override
    public void solicitarRecuperacionPassword(String email) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("No existe un usuario con ese correo"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setExpiracionToken(LocalDateTime.now().plusMinutes(15));

        usuarioRepository.save(usuario);

        String enlaceRestablecimiento = frontendBaseUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
        String mensaje = "Hola " + usuario.getNombre() + ",\n\n" +
                "Has solicitado restablecer tu contraseña en FixLab.\n" +
                "Haz clic en el siguiente enlace para crear una nueva (tienes 15 minutos):\n\n" +
                enlaceRestablecimiento;

        log.info("Enviando correo de recuperación de contraseña a {}", usuario.getEmail());
        emailService.enviarCorreo(usuario.getEmail(), "Recuperación de Contraseña - FixLab", mensaje);
    }

    @Override
    public void cambiarPasswordConToken(String token, String nuevaPassword) throws Exception {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new Exception("Token inválido o no existe"));

        if (usuario.getExpiracionToken().isBefore(LocalDateTime.now())) {
            throw new Exception("El token ha expirado. Solicita uno nuevo.");
        }
        validarFormatoPassword(nuevaPassword);

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setTokenRecuperacion(null);
        usuario.setExpiracionToken(null);

        usuarioRepository.save(usuario);
    }

    @Override
    public void cambiarPassword(String email, String contraseñaActual, String nuevaPassword) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        if (!passwordEncoder.matches(contraseñaActual, usuario.getPassword())) {
            throw new Exception("La contraseña actual no es correcta.");
        }
        validarFormatoPassword(nuevaPassword);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    @Override
    public void asignarNuevaPasswordPorCedula(String cedula, String nuevaPassword) throws Exception {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        validarFormatoPassword(nuevaPassword);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }
}