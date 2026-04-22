package com.software.fixlab.repository;

import com.software.fixlab.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring Boot crea la consulta SQL automáticamente solo con leer el nombre del método:
    // Trae los productos donde stock > 0 y activo = true
    List<Producto> findByStockGreaterThanAndActivoTrue(Integer stock);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= COALESCE(p.stockMinimo, 5)")
    List<Producto> findActivosConStockBajo();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND NOT EXISTS ("
            + "SELECT 1 FROM DetallePedido d JOIN d.pedido ped WHERE d.producto = p AND ped.estado = 'PAGADO' "
            + "AND ped.fechaCreacion >= :d1 AND ped.fechaCreacion < :d2)")
    List<Producto> findActivosSinVentaPagadaEnPeriodo(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);
}