package com.software.fixlab.repository;

import com.software.fixlab.entity.TipoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEquipoRepository extends JpaRepository<TipoEquipo, Integer> {

    Optional<TipoEquipo> findByNombreIgnoreCase(String nombre);
}
