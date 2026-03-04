package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.CategoriaReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import com.software.fixlab.entity.Categoria;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.service.interfaces.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaRespDTO crearCategoria(CategoriaReqDTO dto) throws Exception {
        if (categoriaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new Exception("La categoría '" + dto.getNombre() + "' ya existe.");
        }

        Categoria nuevaCategoria = Categoria.builder()
                .nombre(dto.getNombre())
                .build();

        categoriaRepository.save(nuevaCategoria);

        return new CategoriaRespDTO(nuevaCategoria.getId(), nuevaCategoria.getNombre());
    }

    @Override
    public List<CategoriaRespDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(cat -> new CategoriaRespDTO(cat.getId(), cat.getNombre()))
                .collect(Collectors.toList());
    }
}