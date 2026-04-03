package com.software.fixlab.repository;

import com.software.fixlab.entity.RolUsuario;
import com.software.fixlab.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> { // <-- Cambió a String
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    /** Coincide aunque haya espacios o distinta capitalización (PostgreSQL). */
    @Query(value = "SELECT * FROM usuarios WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1", nativeQuery = true)
    Optional<Usuario> findByEmailNormalized(@Param("email") String email);

    Optional<Usuario> findByTokenRecuperacion(String tokenRecuperacion);

    List<Usuario> findTop20ByRolAndCedulaContainingIgnoreCaseOrderByCedulaAsc(RolUsuario rol, String cedulaFragment);

    List<Usuario> findByRolInOrderByApellidoAscNombreAsc(Collection<RolUsuario> roles);
}