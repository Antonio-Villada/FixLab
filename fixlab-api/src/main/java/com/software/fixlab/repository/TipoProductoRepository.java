package com.software.fixlab.repository;

import com.software.fixlab.entity.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoProductoRepository extends JpaRepository<TipoProducto, Integer> {
    Optional<TipoProducto> findByNombre(String nombre);
}