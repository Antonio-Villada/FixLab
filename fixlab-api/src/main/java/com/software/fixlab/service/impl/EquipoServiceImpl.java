package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.EquipoReqDTO;
import com.software.fixlab.dto.resp.EquipoRespDTO;
import com.software.fixlab.entity.Equipo;
import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.TipoEquipo;
import com.software.fixlab.entity.Usuario;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteEquipoException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.repository.EquipoRepository;
import com.software.fixlab.repository.TipoEquipoRepository;
import com.software.fixlab.repository.UsuarioRepository;
import com.software.fixlab.service.interfaces.EquipoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoServiceImpl implements EquipoService {

    private final EquipoRepository equipoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public EquipoRespDTO crear(EquipoReqDTO dto, String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Usuario propietario;
        if (rol == RolUsuario.CLIENTE) {
            propietario = actor;
        } else if (rol == RolUsuario.ADMIN || rol == RolUsuario.TECNICO || rol == RolUsuario.RECEPCIONISTA) {
            if (dto.getPropietarioCedula() == null || dto.getPropietarioCedula().isBlank()) {
                throw new BadRequestException("Debe indicar la cédula del cliente propietario (propietarioCedula).");
            }
            propietario = usuarioRepository.findById(dto.getPropietarioCedula().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con esa cédula"));
        } else {
            throw new BadRequestException("Rol no autorizado para registrar equipos");
        }

        TipoEquipo tipo = tipoEquipoRepository.findById(dto.getTipoEquipoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de equipo no encontrado"));

        Equipo equipo = Equipo.builder()
                .tipoEquipo(tipo)
                .cliente(propietario)
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .numeroSerie(dto.getNumeroSerie())
                .observaciones(dto.getObservaciones())
                .build();
        return mapear(equipoRepository.save(equipo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoRespDTO> listar(String emailUsuario, RolUsuario rol) {
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (rol == RolUsuario.ADMIN || rol == RolUsuario.TECNICO || rol == RolUsuario.RECEPCIONISTA) {
            return equipoRepository.findAll().stream().map(this::mapear).collect(Collectors.toList());
        }

        if (rol == RolUsuario.CLIENTE) {
            return equipoRepository.findByCliente_CedulaOrderByFechaCreacionDesc(actor.getCedula()).stream()
                    .map(this::mapear)
                    .collect(Collectors.toList());
        }

        throw new BadRequestException("Rol no autorizado");
    }

    @Override
    @Transactional(readOnly = true)
    public EquipoRespDTO obtenerPorId(Integer id, String emailUsuario, RolUsuario rol) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new NoExisteEquipoException("Equipo no encontrado con id: " + id));
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (rol == RolUsuario.ADMIN || rol == RolUsuario.TECNICO || rol == RolUsuario.RECEPCIONISTA) {
            return mapear(equipo);
        }

        if (rol == RolUsuario.CLIENTE && equipo.getCliente().getCedula().equals(actor.getCedula())) {
            return mapear(equipo);
        }

        throw new ResourceNotFoundException("No tiene acceso a este equipo");
    }

    private EquipoRespDTO mapear(Equipo e) {
        Usuario c = e.getCliente();
        return EquipoRespDTO.builder()
                .id(e.getId())
                .tipoEquipoId(e.getTipoEquipo().getId())
                .tipoEquipoNombre(e.getTipoEquipo().getNombre())
                .clienteCedula(c.getCedula())
                .clienteNombre(c.getNombre())
                .clienteApellido(c.getApellido())
                .marca(e.getMarca())
                .modelo(e.getModelo())
                .numeroSerie(e.getNumeroSerie())
                .observaciones(e.getObservaciones())
                .fechaCreacion(e.getFechaCreacion())
                .fechaActualizacion(e.getFechaActualizacion())
                .build();
    }
}
