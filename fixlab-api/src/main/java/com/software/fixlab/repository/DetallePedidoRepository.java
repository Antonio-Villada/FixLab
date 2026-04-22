package com.software.fixlab.repository;

import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    List<DetallePedido> findByPedido(Pedido pedido);

    @Query("SELECT prod.categoria.id, prod.categoria.nombre, SUM(d.cantidad), SUM(d.cantidad * d.precioUnitario) "
            + "FROM DetallePedido d JOIN d.pedido ped JOIN d.producto prod "
            + "WHERE ped.estado = 'PAGADO' AND ped.fechaCreacion >= :d1 AND ped.fechaCreacion < :d2 "
            + "GROUP BY prod.categoria.id, prod.categoria.nombre ORDER BY SUM(d.cantidad * d.precioUnitario) DESC")
    List<Object[]> sumarVentasPorCategoria(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);

    @Query("SELECT prod.id, prod.sku, prod.nombre, prod.categoria.nombre, prod.tipoProducto.nombre, "
            + "SUM(d.cantidad), SUM(d.cantidad * d.precioUnitario) "
            + "FROM DetallePedido d JOIN d.pedido ped JOIN d.producto prod "
            + "WHERE ped.estado = 'PAGADO' AND ped.fechaCreacion >= :d1 AND ped.fechaCreacion < :d2 "
            + "GROUP BY prod.id, prod.sku, prod.nombre, prod.categoria.nombre, prod.tipoProducto.nombre "
            + "ORDER BY SUM(d.cantidad * d.precioUnitario) DESC")
    List<Object[]> topProductosVenta(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2,
            Pageable pageable);

    @Query("SELECT prod.id, prod.sku, prod.nombre, prod.categoria.nombre, COALESCE(SUM(d.cantidad), 0), prod.stock "
            + "FROM DetallePedido d JOIN d.pedido ped JOIN d.producto prod "
            + "WHERE ped.estado = 'PAGADO' AND ped.fechaCreacion >= :d1 AND ped.fechaCreacion < :d2 "
            + "GROUP BY prod.id, prod.sku, prod.nombre, prod.categoria.nombre, prod.stock "
            + "ORDER BY COALESCE(SUM(d.cantidad), 0) DESC")
    List<Object[]> rotacionPorProducto(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);
}