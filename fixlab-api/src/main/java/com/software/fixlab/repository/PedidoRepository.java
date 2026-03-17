package com.software.fixlab.repository;

import com.software.fixlab.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByCliente_Cedula(String cedula);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(String estado);

    List<Pedido> findByIdInOrderByFechaCreacionDesc(List<Integer> ids);
}