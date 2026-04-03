package com.software.fixlab.repository;

import com.software.fixlab.entity.ReparacionHistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReparacionHistorialEstadoRepository extends JpaRepository<ReparacionHistorialEstado, Long> {

    List<ReparacionHistorialEstado> findByReparacion_IdOrderByFechaCambioAsc(Integer reparacionId);
}
