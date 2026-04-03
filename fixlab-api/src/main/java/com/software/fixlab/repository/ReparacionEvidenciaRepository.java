package com.software.fixlab.repository;

import com.software.fixlab.entity.ReparacionEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReparacionEvidenciaRepository extends JpaRepository<ReparacionEvidencia, Integer> {

    List<ReparacionEvidencia> findByReparacion_IdOrderByOrdenAscIdAsc(Integer reparacionId);
}
