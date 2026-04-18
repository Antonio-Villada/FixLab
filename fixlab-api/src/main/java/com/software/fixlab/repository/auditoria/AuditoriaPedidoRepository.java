package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaPedidoRepository extends JpaRepository<AuditoriaPedido, Long> {}
