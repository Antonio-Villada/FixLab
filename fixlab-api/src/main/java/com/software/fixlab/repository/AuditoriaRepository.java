package com.software.fixlab.repository;

import com.software.fixlab.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
}