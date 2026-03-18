package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.TipoProductoReqDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteTipoProductoException;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.TipoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoProductoServiceImpl implements TipoProductoService {

    private final TipoProductoRepository tipoProductoRepository;

    @Override
    @Transactional
    public TipoProductoRespDTO crearTipoProducto(TipoProductoReqDTO dto) {
        if (tipoProductoRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new BadRequestException("El tipo de producto '" + dto.getNombre() + "' ya existe.");
        }

        TipoProducto nuevoTipo = TipoProducto.builder()
                .nombre(dto.getNombre())
                .build();

        tipoProductoRepository.save(nuevoTipo);

        return new TipoProductoRespDTO(nuevoTipo.getId(), nuevoTipo.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoProductoRespDTO> obtenerTodos() {
        return tipoProductoRepository.findAll().stream()
                .map(tipo -> new TipoProductoRespDTO(tipo.getId(), tipo.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoProductoRespDTO obtenerPorId(Integer id) {
        TipoProducto tipo = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new NoExisteTipoProductoException("Tipo de producto no encontrado con ID: " + id));
        return new TipoProductoRespDTO(tipo.getId(), tipo.getNombre());
    }

    @Override
    @Transactional
    public TipoProductoRespDTO actualizarTipoProducto(Integer id, TipoProductoReqDTO dto) {
        // 1. Validar existencia
        TipoProducto tipo = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new NoExisteTipoProductoException("Tipo de producto no encontrado con ID: " + id));

        // 2. Validar que el nuevo nombre no lo tenga otro registro
        Optional<TipoProducto> tipoExistente = tipoProductoRepository.findByNombre(dto.getNombre());
        if (tipoExistente.isPresent() && !tipoExistente.get().getId().equals(id)) {
            throw new BadRequestException("El nombre '" + dto.getNombre() + "' ya está en uso por otro tipo de producto.");
        }

        // 3. Actualizar
        tipo.setNombre(dto.getNombre());
        tipoProductoRepository.save(tipo);

        return new TipoProductoRespDTO(tipo.getId(), tipo.getNombre());
    }

    @Override
    @Transactional
    public void eliminarTipoProducto(Integer id) {
        TipoProducto tipo = tipoProductoRepository.findById(id)
                .orElseThrow(() -> new NoExisteTipoProductoException("Tipo de producto no encontrado con ID: " + id));

        tipoProductoRepository.delete(tipo);
    }
}