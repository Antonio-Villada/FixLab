package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.UsuarioUpdateReqDTO;
import com.software.fixlab.dto.resp.ClienteSugerenciaRespDTO;
import com.software.fixlab.dto.resp.StaffTallerAsignableRespDTO;
import com.software.fixlab.dto.resp.UsuarioRespDTO;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.mapper.UsuarioMapper;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.CloudinaryService;
import com.software.fixlab.service.interfaces.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRespDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSugerenciaRespDTO> buscarSugerenciasClientesPorCedula(String fragmento) {
        if (fragmento == null) {
            return List.of();
        }
        String q = fragmento.trim();
        if (q.length() < 2) {
            return List.of();
        }
        return usuarioRepository
                .findTop20ByRolAndCedulaContainingIgnoreCaseOrderByCedulaAsc(RolUsuario.CLIENTE, q)
                .stream()
                .map(u -> ClienteSugerenciaRespDTO.builder()
                        .cedula(u.getCedula())
                        .nombre(u.getNombre())
                        .apellido(u.getApellido())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffTallerAsignableRespDTO> listarStaffAsignableComoTecnico() {
        return usuarioRepository
                .findByRolInOrderByApellidoAscNombreAsc(EnumSet.of(RolUsuario.TECNICO, RolUsuario.ADMIN))
                .stream()
                .map(u -> StaffTallerAsignableRespDTO.builder()
                        .cedula(u.getCedula())
                        .nombre(u.getNombre())
                        .apellido(u.getApellido())
                        .rol(u.getRol())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRespDTO obtenerPorCedula(String cedula) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRespDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioRespDTO actualizarUsuario(String cedula, UsuarioUpdateReqDTO dto) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));

        // Actualizamos solo los campos permitidos
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setTelefono(dto.getTelefono());
        if (dto.getFotoUrl() != null) {
            usuario.setFotoUrl(dto.getFotoUrl());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuarioActualizado);
    }

    @Override
    @Transactional
    public UsuarioRespDTO actualizarMiPerfil(String email, UsuarioUpdateReqDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
        if (dto.getApellido() != null) usuario.setApellido(dto.getApellido());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());
        if (dto.getFotoUrl() != null) usuario.setFotoUrl(dto.getFotoUrl());

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDto(actualizado);
    }

    @Override
    @Transactional
    public UsuarioRespDTO subirFotoPerfil(String email, MultipartFile foto) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        if (foto == null || foto.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar una imagen");
        }
        String urlFoto = cloudinaryService.subirImagen(foto);
        usuario.setFotoUrl(urlFoto);
        usuarioRepository.save(usuario);
        return usuarioMapper.toDto(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(String cedula) {
        Usuario usuario = usuarioRepository.findById(cedula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con cédula: " + cedula));

        usuarioRepository.delete(usuario);
    }
}