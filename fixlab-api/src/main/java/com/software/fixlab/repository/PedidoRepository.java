package com.software.fixlab.repository;

import com.software.fixlab.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Por ahora no necesitamos consultas personalizadas,
    // JpaRepository ya incluye el método save() que usamos en el servicio.
}