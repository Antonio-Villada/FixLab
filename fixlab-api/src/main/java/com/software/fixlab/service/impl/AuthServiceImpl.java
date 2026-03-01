package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.LoginReqDTO;
import com.software.fixlab.dto.req.RegistroReqDTO;
import com.software.fixlab.dto.resp.MensajeRespDTO;
import com.software.fixlab.dto.resp.TokenRespDTO;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.mapper.UsuarioMapper;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper; // <-- Inyectamos el Mapper
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // <-- Nueva inyección


    @Override
    @Transactional
    public MensajeRespDTO registrarCliente(RegistroReqDTO dto) throws Exception {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new Exception("El correo electrónico ya se encuentra registrado.");
        }

        // Usamos el Mapper para delegar la transformación
        Usuario nuevoUsuario = usuarioMapper.toEntity(dto);

        usuarioRepository.save(nuevoUsuario);

        return new MensajeRespDTO("Usuario registrado exitosamente");
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

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        // Generamos el token real usando nuestro nuevo servicio
        String jwtToken = jwtService.generarToken(usuario);
        return new TokenRespDTO(jwtToken, usuario.getRol().name());
    }

    private void manejarIntentoFallido(Usuario usuario) {
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

        if (usuario.getIntentosFallidos() >= 3) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
            usuario.setIntentosFallidos(0);
        }
        usuarioRepository.save(usuario);
    }
}