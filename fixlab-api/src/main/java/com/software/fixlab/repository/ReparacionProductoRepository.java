package com.software.fixlab.repository;

import com.software.fixlab.entity.ReparacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReparacionProductoRepository extends JpaRepository<ReparacionProducto, Integer> {

    List<ReparacionProducto> findByReparacion_IdOrderByIdAsc(Integer reparacionId);

    void deleteByReparacion_Id(Integer reparacionId);
}
