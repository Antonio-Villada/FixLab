package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.TipoProductoReqDTO;
import com.software.fixlab.dto.resp.TipoProductoRespDTO;
import com.software.fixlab.entity.TipoProducto;
import com.software.fixlab.repository.TipoProductoRepository;
import com.software.fixlab.service.interfaces.TipoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoProductoServiceImpl implements TipoProductoService {

    private final TipoProductoRepository tipoProductoRepository;

    @Override
    public TipoProductoRespDTO crearTipoProducto(TipoProductoReqDTO dto) throws Exception {
        if (tipoProductoRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new Exception("El tipo de producto '" + dto.getNombre() + "' ya existe.");
        }

        TipoProducto nuevoTipo = TipoProducto.builder()
                .nombre(dto.getNombre())
                .build();

        tipoProductoRepository.save(nuevoTipo);

        return new TipoProductoRespDTO(nuevoTipo.getId(), nuevoTipo.getNombre());
    }

    @Override
    public List<TipoProductoRespDTO> obtenerTodos() {
        return tipoProductoRepository.findAll().stream()
                .map(tipo -> new TipoProductoRespDTO(tipo.getId(), tipo.getNombre()))
                .collect(Collectors.toList());
    }
}