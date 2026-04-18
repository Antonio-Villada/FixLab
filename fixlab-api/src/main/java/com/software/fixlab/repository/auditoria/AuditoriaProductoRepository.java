package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaProductoRepository extends JpaRepository<AuditoriaProducto, Long> {}
