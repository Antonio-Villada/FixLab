package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaReparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaReparacionRepository extends JpaRepository<AuditoriaReparacion, Long> {}
