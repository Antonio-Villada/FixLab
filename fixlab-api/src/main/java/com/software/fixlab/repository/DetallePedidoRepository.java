package com.software.fixlab.repository;

import com.software.fixlab.entity.DetallePedido;
import com.software.fixlab.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    List<DetallePedido> findByPedido(Pedido pedido);

    /** Producto id y suma de cantidad vendida (todos los pedidos), ordenado por más vendidos. */
    @Query("SELECT d.producto.id, SUM(d.cantidad) FROM DetallePedido d GROUP BY d.producto.id ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductoIdAndTotalCantidadVendida();

    /** IDs de productos que aparecen en pedidos con el estado dado. */
    @Query("SELECT DISTINCT d.producto.id FROM DetallePedido d WHERE d.pedido.estado = :estado")
    List<Long> findDistinctProductoIdsByPedidoEstado(@Param("estado") String estado);

    /** Producto id y suma de cantidad vendida, solo productos de la categoría dada. */
    @Query("SELECT d.producto.id, SUM(d.cantidad) FROM DetallePedido d WHERE d.producto.categoria.id = :categoriaId GROUP BY d.producto.id ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductoIdAndTotalCantidadVendidaByCategoriaId(@Param("categoriaId") Long categoriaId);

    /** IDs de pedidos que tienen al menos un producto de la categoría dada. */
    @Query("SELECT DISTINCT d.pedido.id FROM DetallePedido d WHERE d.producto.categoria.id = :categoriaId")
    List<Integer> findDistinctPedidoIdsByProductoCategoriaId(@Param("categoriaId") Long categoriaId);
}