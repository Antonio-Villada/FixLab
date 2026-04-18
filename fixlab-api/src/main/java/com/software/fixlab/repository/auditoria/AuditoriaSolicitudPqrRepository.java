package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaSolicitudPqr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaSolicitudPqrRepository extends JpaRepository<AuditoriaSolicitudPqr, Long> {}
