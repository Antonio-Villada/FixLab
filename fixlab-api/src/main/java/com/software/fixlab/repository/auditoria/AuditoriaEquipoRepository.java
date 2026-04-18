package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaEquipoRepository extends JpaRepository<AuditoriaEquipo, Long> {}
