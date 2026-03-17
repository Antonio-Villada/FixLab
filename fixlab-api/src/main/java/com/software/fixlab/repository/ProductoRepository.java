package com.software.fixlab.repository;

import com.software.fixlab.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByStockGreaterThanAndActivoTrue(Integer stock);

    List<Producto> findByIdInOrderByNombre(List<Long> ids);

    List<Producto> findByStockOrderByNombre(Integer stock);

    List<Producto> findByActivoOrderByNombre(Boolean activo);
}