package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaAuthRepository extends JpaRepository<AuditoriaAuth, Long> {}
