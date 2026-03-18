package com.software.fixlab.service.impl;

import com.software.fixlab.dto.req.CategoriaReqDTO;
import com.software.fixlab.dto.resp.CategoriaRespDTO;
import com.software.fixlab.entity.Categoria;
import com.software.fixlab.exception.BadRequestException;
import com.software.fixlab.exception.NoExisteCategoriaException;
import com.software.fixlab.repository.CategoriaRepository;
import com.software.fixlab.service.interfaces.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public CategoriaRespDTO crearCategoria(CategoriaReqDTO dto) {
        if (categoriaRepository.findByNombre(dto.getNombre()).isPresent()) {
            throw new BadRequestException("La categoría '" + dto.getNombre() + "' ya existe.");
        }

        Categoria nuevaCategoria = Categoria.builder()
                .nombre(dto.getNombre())
                .build();

        categoriaRepository.save(nuevaCategoria);

        return new CategoriaRespDTO(nuevaCategoria.getId(), nuevaCategoria.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaRespDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(cat -> new CategoriaRespDTO(cat.getId(), cat.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaRespDTO obtenerPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NoExisteCategoriaException("Categoría no encontrada con ID: " + id));
        return new CategoriaRespDTO(categoria.getId(), categoria.getNombre());
    }

    @Override
    @Transactional
    public CategoriaRespDTO actualizarCategoria(Integer id, CategoriaReqDTO dto) {
        // 1. Buscamos si existe la categoría a actualizar
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NoExisteCategoriaException("Categoría no encontrada con ID: " + id));

        // 2. Validar que el nuevo nombre no esté ocupado por OTRA categoría distinta
        Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(dto.getNombre());
        if (categoriaExistente.isPresent() && !categoriaExistente.get().getId().equals(id)) {
            throw new BadRequestException("El nombre '" + dto.getNombre() + "' ya está en uso por otra categoría.");
        }

        // 3. Actualizamos y guardamos
        categoria.setNombre(dto.getNombre());
        categoriaRepository.save(categoria);

        return new CategoriaRespDTO(categoria.getId(), categoria.getNombre());
    }

    @Override
    @Transactional
    public void eliminarCategoria(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new NoExisteCategoriaException("Categoría no encontrada con ID: " + id));

        categoriaRepository.delete(categoria);
    }
}