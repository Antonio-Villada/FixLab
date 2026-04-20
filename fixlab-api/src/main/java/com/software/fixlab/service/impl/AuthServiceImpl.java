package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.*;
import com.software.fixlab.dto.resp.LoginPaso1RespDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRecuperacionRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.util.EmailMaskUtil;
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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
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
    public UsuarioRespDTO registrarClienteMostrador(ClienteMostradorReqDTO dto) throws Exception {
        if (DisposableEmailValidator.isDisposable(dto.getEmail())) {
            throw new Exception(
                    "No se permite el registro con correos temporales o desechables. Por favor utiliza un correo electrónico permanente.");
        }
        String email = dto.getEmail().trim();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new Exception("El correo electrónico ya se encuentra registrado.");
        }
        String cedula = dto.getCedula().trim();
        if (usuarioRepository.existsById(cedula)) {
            throw new Exception("La cédula ya se encuentra registrada en el sistema.");
        }

        String passwordTemporalPlano = generarPasswordTemporalSeguro();
        validarFormatoPassword(passwordTemporalPlano);

        String tel = dto.getTelefono();
        if (tel != null) {
            tel = tel.trim();
            if (tel.isEmpty()) {
                tel = null;
            }
        }

        Usuario nuevoUsuario = Usuario.builder()
                .cedula(cedula)
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .email(email)
                .telefono(tel)
                .password(passwordEncoder.encode(passwordTemporalPlano))
                .rol(RolUsuario.CLIENTE)
                .intentosFallidos(0)
                .correoVerificado(true)
                .requiereCambioPassword(true)
                .build();

        usuarioRepository.save(nuevoUsuario);
        emailService.enviarPasswordTemporalRegistroMostrador(email, nuevoUsuario.getNombre(), passwordTemporalPlano);
        return usuarioMapper.toDto(nuevoUsuario);
    }

    /** Contraseña aleatoria que cumple {@link #validarFormatoPassword(String)}. */
    private static String generarPasswordTemporalSeguro() {
        SecureRandom r = new SecureRandom();
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String special = "@#$%&*!?";
        String all = upper + lower + digits + special;
        for (int intento = 0; intento < 50; intento++) {
            StringBuilder sb = new StringBuilder(16);
            sb.append(upper.charAt(r.nextInt(upper.length())));
            sb.append(lower.charAt(r.nextInt(lower.length())));
            sb.append(digits.charAt(r.nextInt(digits.length())));
            sb.append(special.charAt(r.nextInt(special.length())));
            for (int i = 4; i < 16; i++) {
                sb.append(all.charAt(r.nextInt(all.length())));
            }
            char[] arr = sb.toString().toCharArray();
            for (int i = arr.length - 1; i > 0; i--) {
                int j = r.nextInt(i + 1);
                char t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }
            String candidate = new String(arr);
            if (PASSWORD_PATTERN.matcher(candidate).matches()) {
                return candidate;
            }
        }
        return "Fx7!" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "@";
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

        RolUsuario rolEmp = dto.getRol();
        if (rolEmp != RolUsuario.ADMIN && rolEmp != RolUsuario.TECNICO && rolEmp != RolUsuario.RECEPCIONISTA) {
            throw new Exception("El rol de empleado debe ser ADMIN, TECNICO o RECEPCIONISTA");
        }

        Usuario nuevoEmpleado = Usuario.builder()
                .cedula(dto.getCedula())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .rol(dto.getRol()) // ADMIN, TECNICO o RECEPCIONISTA
                .intentosFallidos(0)
                .correoVerificado(true) // El técnico nace con la cuenta activa
                .build();

        usuarioRepository.save(nuevoEmpleado);

        return new MensajeRespDTO("Empleado registrado exitosamente como " + dto.getRol());
    }

    private static final int LOGIN_2FA_MAX_INTENTOS_CODIGO = 5;
    private static final int LOGIN_2FA_CODIGO_MINUTOS = 15;

    @Override
    @Transactional
    public LoginPaso1RespDTO login(LoginReqDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new Exception("Credenciales incorrectas"));

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new Exception("Cuenta bloqueada por múltiples intentos. Intente de nuevo más tarde.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            manejarIntentoFallido(usuario);
            throw new Exception("Credenciales incorrectas");
        }

        if (!usuario.isActivo()) {
            throw new Exception("Tu cuenta fue eliminada de FixLab. Si crees que es un error, contacta a soporte.");
        }

        if (usuario.getRol() == RolUsuario.CLIENTE && !usuario.isCorreoVerificado()) {
            throw new Exception("Debes verificar tu correo antes de iniciar sesión. Revisa el código de 6 dígitos que enviamos a tu email.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        boolean codigoAunVigente = usuario.getCodigoLogin2fa() != null
                && usuario.getExpiracionCodigoLogin2fa() != null
                && usuario.getExpiracionCodigoLogin2fa().isAfter(ahora);
        if (codigoAunVigente
                && usuario.getUltimoEnvioCodigoLogin2fa() != null
                && ahora.isBefore(usuario.getUltimoEnvioCodigoLogin2fa().plusSeconds(60))) {
            throw new Exception("Ya se envió un código reciente. Revisa tu correo o espera un minuto para solicitar otro.");
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        String codigo = String.format("%06d", new java.util.Random().nextInt(999999));
        usuario.setCodigoLogin2fa(codigo);
        usuario.setExpiracionCodigoLogin2fa(ahora.plusMinutes(LOGIN_2FA_CODIGO_MINUTOS));
        usuario.setIntentosCodigoLogin2fa(0);
        usuario.setUltimoEnvioCodigoLogin2fa(ahora);
        usuarioRepository.save(usuario);

        emailService.enviarCodigoLogin2fa(usuario.getEmail(), usuario.getNombre(), codigo);

        return new LoginPaso1RespDTO("CODIGO_ENVIADO", EmailMaskUtil.enmascarar(usuario.getEmail()));
    }

    @Override
    @Transactional
    public TokenRespDTO loginVerificarCodigo(LoginVerificarCodigoReqDTO dto) throws Exception {
        String email = dto.getEmail().trim();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Solicitud inválida. Vuelve a iniciar sesión desde el principio."));

        if (!usuario.isActivo()) {
            throw new Exception("Tu cuenta fue eliminada de FixLab. Si crees que es un error, contacta a soporte.");
        }

        if (usuario.getCodigoLogin2fa() == null || usuario.getExpiracionCodigoLogin2fa() == null) {
            throw new Exception("No hay un código pendiente. Inicia sesión de nuevo con tu correo y contraseña.");
        }
        if (usuario.getExpiracionCodigoLogin2fa().isBefore(LocalDateTime.now())) {
            usuario.setCodigoLogin2fa(null);
            usuario.setExpiracionCodigoLogin2fa(null);
            usuario.setUltimoEnvioCodigoLogin2fa(null);
            usuarioRepository.save(usuario);
            throw new Exception("El código ha expirado. Inicia sesión de nuevo para recibir uno nuevo.");
        }

        if (!usuario.getCodigoLogin2fa().equals(dto.getCodigo())) {
            usuario.setIntentosCodigoLogin2fa(usuario.getIntentosCodigoLogin2fa() + 1);
            if (usuario.getIntentosCodigoLogin2fa() >= LOGIN_2FA_MAX_INTENTOS_CODIGO) {
                usuario.setCodigoLogin2fa(null);
                usuario.setExpiracionCodigoLogin2fa(null);
                usuario.setUltimoEnvioCodigoLogin2fa(null);
                usuario.setIntentosCodigoLogin2fa(0);
            }
            usuarioRepository.save(usuario);
            if (usuario.getIntentosCodigoLogin2fa() == 0 && usuario.getCodigoLogin2fa() == null) {
                throw new Exception("Demasiados intentos fallidos. Inicia sesión de nuevo desde el principio.");
            }
            throw new Exception("El código es incorrecto.");
        }

        usuario.setCodigoLogin2fa(null);
        usuario.setExpiracionCodigoLogin2fa(null);
        usuario.setUltimoEnvioCodigoLogin2fa(null);
        usuario.setIntentosCodigoLogin2fa(0);
        usuarioRepository.save(usuario);

        String jwtToken = jwtService.generarToken(usuario);
        return new TokenRespDTO(jwtToken, usuario.getRol().name(), usuario.isRequiereCambioPassword());
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

        if (!usuario.isActivo()) {
            throw new Exception("No se puede completar la verificación de esta cuenta.");
        }

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
        String raw = email == null ? "" : email.trim();
        if (raw.isEmpty()) {
            return;
        }

        Optional<Usuario> ou = usuarioRepository.findByEmail(raw);
        if (ou.isEmpty()) {
            ou = usuarioRepository.findByEmailIgnoreCase(raw);
        }
        if (ou.isEmpty()) {
            ou = usuarioRepository.findByEmailNormalized(raw);
        }
        if (ou.isEmpty()) {
            log.info("Recuperación de contraseña: correo no registrado (respuesta genérica al cliente).");
            return;
        }

        Usuario usuario = ou.get();
        if (!usuario.isActivo()) {
            log.info("Recuperación de contraseña: cuenta inactiva (respuesta genérica al cliente).");
            return;
        }
        String codigo = String.format("%06d", new java.util.Random().nextInt(999999));
        usuario.setTokenRecuperacion(codigo);
        usuario.setExpiracionToken(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        String destino = usuario.getEmail() != null ? usuario.getEmail().trim() : raw;
        log.info("Recuperación: enviando código de 6 dígitos a [{}]", destino);
        emailService.enviarCodigoRecuperacionPassword(destino, usuario.getNombre(), codigo);
    }

    @Override
    @Transactional
    public TokenRecuperacionRespDTO verificarCodigoRecuperacion(VerificarCodigoRecuperacionReqDTO dto) throws Exception {
        String emailRaw = (dto.getEmail() == null) ? "" : dto.getEmail().trim();
        String codigoRaw = (dto.getCodigo() == null) ? "" : String.valueOf(dto.getCodigo()).trim();
        log.info("Verificar código recuperación: email=[{}], codigo=[{}]", emailRaw, codigoRaw);
        if (emailRaw.isEmpty() || codigoRaw.isEmpty()) {
            throw new Exception("Email y código son obligatorios.");
        }

        Optional<Usuario> ou = usuarioRepository.findByEmail(emailRaw);
        if (ou.isEmpty()) {
            ou = usuarioRepository.findByEmailIgnoreCase(emailRaw);
        }
        if (ou.isEmpty()) {
            ou = usuarioRepository.findByEmailNormalized(emailRaw);
        }
        Usuario usuario = ou.orElseThrow(() -> new Exception("Código incorrecto o expirado."));

        if (!usuario.isActivo()) {
            throw new Exception("Código incorrecto o expirado.");
        }

        if (usuario.getTokenRecuperacion() == null || usuario.getExpiracionToken() == null) {
            log.warn("Usuario sin código pendiente: {}", usuario.getEmail());
            throw new Exception("No hay un código pendiente. Solicita uno nuevo.");
        }
        if (usuario.getExpiracionToken().isBefore(LocalDateTime.now())) {
            usuario.setTokenRecuperacion(null);
            usuario.setExpiracionToken(null);
            usuarioRepository.save(usuario);
            throw new Exception("El código ha expirado. Solicita uno nuevo.");
        }
        String codigoGuardado = usuario.getTokenRecuperacion().trim();
        String codigoIngresado = codigoRaw.replaceAll("\\s", "");
        if (!codigoGuardado.equals(codigoIngresado)) {
            log.warn("Código no coincide: esperado=[{}], recibido=[{}]", codigoGuardado, codigoIngresado);
            throw new Exception("El código es incorrecto.");
        }

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setExpiracionToken(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);

        return new TokenRecuperacionRespDTO(token);
    }

    @Override
    public void cambiarPasswordConToken(String token, String nuevaPassword) throws Exception {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new Exception("Token inválido o no existe"));

        if (!usuario.isActivo()) {
            throw new Exception("Token inválido o no existe");
        }

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
        if (!usuario.isActivo()) {
            throw new Exception("Usuario no encontrado");
        }
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

    @Override
    @Transactional
    public void completarCambioPasswordPrimerAcceso(String email, String nuevaPassword) throws Exception {
        String em = email == null ? "" : email.trim();
        Usuario usuario = usuarioRepository.findByEmail(em)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        if (!usuario.isActivo()) {
            throw new Exception("Tu cuenta no está disponible.");
        }
        if (!usuario.isRequiereCambioPassword()) {
            throw new Exception("No tienes un cambio de contraseña pendiente. Usa la opción de cambiar contraseña en tu perfil.");
        }
        validarFormatoPassword(nuevaPassword);
        if (passwordEncoder.matches(nuevaPassword, usuario.getPassword())) {
            throw new Exception("La nueva contraseña debe ser distinta de la contraseña temporal que recibiste por correo.");
        }
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);
    }
}