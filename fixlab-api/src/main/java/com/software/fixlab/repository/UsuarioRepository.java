package com.software.fixlab.repository;

import com.software.fixlab.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
<<<<<<< HEAD
public interface UsuarioRepository extends JpaRepository<Usuario, String> { // <-- Cambió a String
    Optional<Usuario> findByEmail(String email);
=======
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);
>>>>>>> ebc21313413cb4bf66ee58a55f8bed5b9e914097
}