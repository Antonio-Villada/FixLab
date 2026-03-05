package com.software.fixlab.mapper;

import com.software.fixlab.dto.req.RegistroReqDTO;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor // <-- Agregamos esto para inyectar dependencias
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder; // <-- Inyectamos el encriptador

    public Usuario toEntity(RegistroReqDTO dto) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .cedula(dto.getCedula().trim())
                .nombre(dto.getNombre())
                .apellidos(dto.getApellidos())
                .direccion(dto.getDireccion() != null ? dto.getDireccion() : "")
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .rol(RolUsuario.CLIENTE)
                .intentosFallidos(0)
                .build();
    }
}