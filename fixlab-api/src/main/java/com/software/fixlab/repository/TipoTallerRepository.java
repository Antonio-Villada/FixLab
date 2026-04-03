package com.software.fixlab.repository;

import com.software.fixlab.entity.TipoTaller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoTallerRepository extends JpaRepository<TipoTaller, Integer> {

    Optional<TipoTaller> findByNombreIgnoreCase(String nombre);
}
