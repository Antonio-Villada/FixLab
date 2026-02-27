package com.software.fixlab.repository;

import com.software.fixlab.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Método clave para el login y para evitar duplicidad en el registro
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}