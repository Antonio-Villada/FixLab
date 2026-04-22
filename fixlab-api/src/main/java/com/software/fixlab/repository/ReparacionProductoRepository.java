package com.software.fixlab.repository;

import com.software.fixlab.entity.ReparacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReparacionProductoRepository extends JpaRepository<ReparacionProducto, Integer> {

    List<ReparacionProducto> findByReparacion_IdOrderByIdAsc(Integer reparacionId);

    void deleteByReparacion_Id(Integer reparacionId);

    @Query("SELECT d.producto.id, d.producto.sku, d.producto.nombre, SUM(d.cantidad), "
            + "SUM(d.cantidad * d.precioUnitarioSnapshot) FROM ReparacionProducto d JOIN d.reparacion r "
            + "WHERE r.fechaCreacion >= :d1 AND r.fechaCreacion < :d2 "
            + "GROUP BY d.producto.id, d.producto.sku, d.producto.nombre ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> sumarRepuestosPorProductoEnPeriodo(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);
}
