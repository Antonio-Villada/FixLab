package com.software.fixlab.repository.auditoria;

import com.software.fixlab.entity.auditoria.AuditoriaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaUsuarioRepository extends JpaRepository<AuditoriaUsuario, Long> {}
