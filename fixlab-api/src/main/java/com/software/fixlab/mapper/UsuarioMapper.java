package com.software.fixlab.mapper;

import com.software.fixlab.dto.req.RegistroReqDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final PasswordEncoder passwordEncoder;

    public Usuario toEntity(RegistroReqDTO dto) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .cedula(dto.getCedula()) // Aseguramos que la cédula se mapee si viene en el registro
                .nombre(dto.getNombre())
                .apellido(dto.getApellido()) // Faltaba el apellido en tu versión original
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .telefono(dto.getTelefono())
                .rol(RolUsuario.CLIENTE)
                .intentosFallidos(0)
                .correoVerificado(false)
                .build();
    }

    // NUEVO: Método para convertir la Entidad al DTO de Respuesta para el CRUD
    public UsuarioRespDTO toDto(Usuario entity) {
        if (entity == null) {
            return null;
        }

        return UsuarioRespDTO.builder()
                .cedula(entity.getCedula())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .email(entity.getEmail())
                .telefono(entity.getTelefono())
                .fotoUrl(entity.getFotoUrl())
                .rol(entity.getRol())
                .correoVerificado(entity.isCorreoVerificado())
                .requiereCambioPassword(entity.isRequiereCambioPassword())
                .build();
    }
}