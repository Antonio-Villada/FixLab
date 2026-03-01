package com.software.fixlab.repository;

import com.software.fixlab.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring Boot crea la consulta SQL automáticamente solo con leer el nombre del método:
    // Trae los productos donde stock > 0 y activo = true
    List<Producto> findByStockGreaterThanAndActivoTrue(Integer stock);
}