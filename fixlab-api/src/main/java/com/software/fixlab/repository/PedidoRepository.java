package com.software.fixlab.repository;

import com.software.fixlab.entity.Pedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    // Nos servirá más adelante para que un cliente vea su historial de compras
    List<Pedido> findByCliente_Cedula(String cedula);

    List<Pedido> findByCliente_CedulaOrderByFechaCreacionDesc(String cedula);

    Optional<Pedido> findByIdAndCliente_Cedula(Integer id, String clienteCedula);

    boolean existsByCliente_CedulaAndEstadoNotIn(String clienteCedula, Collection<String> estados);

    @Query("SELECT p.estado, COUNT(p) FROM Pedido p WHERE p.fechaCreacion >= :d1 AND p.fechaCreacion < :d2 GROUP BY p.estado")
    List<Object[]> contarPedidosPorEstadoEnPeriodo(@Param("d1") LocalDateTime d1, @Param("d2") LocalDateTime d2);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado = :estado AND p.fechaCreacion >= :d1 AND p.fechaCreacion < :d2")
    Double sumarTotalPedidosPorEstadoEnPeriodo(
            @Param("estado") String estado,
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.fechaCreacion >= :d1 AND p.fechaCreacion < :d2")
    Double sumarTotalTodosPedidosEnPeriodo(@Param("d1") LocalDateTime d1, @Param("d2") LocalDateTime d2);

    List<Pedido> findTop500ByEstadoInOrderByFechaCreacionDesc(Collection<String> estados);

    long countByFechaCreacionGreaterThanEqualAndFechaCreacionBefore(LocalDateTime d1, LocalDateTime d2);

    @Query("SELECT COUNT(DISTINCT p.cliente.cedula) FROM Pedido p WHERE p.estado = 'PAGADO' "
            + "AND p.fechaCreacion >= :d1 AND p.fechaCreacion < :d2")
    long contarClientesDistintosConPedidoPagado(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2);

    @Query("SELECT p.cliente.cedula, CONCAT(CONCAT(p.cliente.nombre, ' '), p.cliente.apellido), COUNT(p), COALESCE(SUM(p.total), 0) "
            + "FROM Pedido p WHERE p.estado = 'PAGADO' AND p.fechaCreacion >= :d1 AND p.fechaCreacion < :d2 "
            + "GROUP BY p.cliente.cedula, p.cliente.nombre, p.cliente.apellido ORDER BY COALESCE(SUM(p.total), 0) DESC")
    List<Object[]> topClientesPorMontoPagado(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2,
            Pageable pageable);
}