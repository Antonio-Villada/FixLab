package com.software.fixlab.repository;

import com.software.fixlab.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> { // <-- Cambió a String
    Optional<Usuario> findByEmail(String email);
}