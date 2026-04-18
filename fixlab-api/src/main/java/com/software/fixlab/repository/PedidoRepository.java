package com.software.fixlab.repository;

import com.software.fixlab.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}