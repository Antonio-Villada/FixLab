package com.software.fixlab.repository;

import com.software.fixlab.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    List<Equipo> findByCliente_CedulaOrderByFechaCreacionDesc(String clienteCedula);

    Optional<Equipo> findByIdAndCliente_Cedula(Integer id, String clienteCedula);
}
