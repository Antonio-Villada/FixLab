package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.TipoEquipoReqDTO;
import com.software.fixlab.dto.resp.TipoEquipoRespDTO;
import com.software.fixlab.entity.TipoEquipo;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.ResourceNotFoundException;
import com.software.fixlab.repository.EquipoRepository;
import com.software.fixlab.repository.TipoEquipoRepository;
import com.software.fixlab.service.interfaces.TipoEquipoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEquipoServiceImpl implements TipoEquipoService {

    private final TipoEquipoRepository tipoEquipoRepository;
    private final EquipoRepository equipoRepository;

    @Override
    @Transactional
    public TipoEquipoRespDTO crear(TipoEquipoReqDTO dto) {
        String nombre = dto.getNombre().trim();
        if (tipoEquipoRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new BadRequestException("Ya existe un tipo de equipo con el nombre '" + nombre + "'.");
        }
        TipoEquipo saved = tipoEquipoRepository.save(TipoEquipo.builder().nombre(nombre).build());
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEquipoRespDTO> listarTodos() {
        return tipoEquipoRepository.findAll().stream()
                .sorted(Comparator.comparing(TipoEquipo::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEquipoRespDTO obtenerPorId(Integer id) {
        TipoEquipo t = tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de equipo no encontrado con id: " + id));
        return toDto(t);
    }

    @Override
    @Transactional
    public TipoEquipoRespDTO actualizar(Integer id, TipoEquipoReqDTO dto) {
        TipoEquipo tipo = tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de equipo no encontrado con id: " + id));
        String nombre = dto.getNombre().trim();
        tipoEquipoRepository.findByNombreIgnoreCase(nombre)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BadRequestException("El nombre '" + nombre + "' ya está en uso por otro tipo de equipo.");
                });
        tipo.setNombre(nombre);
        return toDto(tipoEquipoRepository.save(tipo));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoEquipo tipo = tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de equipo no encontrado con id: " + id));
        long enUso = equipoRepository.countByTipoEquipo_Id(id);
        if (enUso > 0) {
            throw new BadRequestException("No se puede eliminar: hay equipos asociados a este tipo (" + enUso + ").");
        }
        tipoEquipoRepository.delete(tipo);
    }

    private TipoEquipoRespDTO toDto(TipoEquipo t) {
        return TipoEquipoRespDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .fechaCreacion(t.getFechaCreacion())
                .build();
    }
}
