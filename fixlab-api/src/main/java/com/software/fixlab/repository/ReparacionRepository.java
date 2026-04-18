package com.software.fixlab.repository;

import com.software.fixlab.entity.EstadoReparacion;
import com.software.fixlab.entity.Reparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReparacionRepository extends JpaRepository<Reparacion, Integer> {

    Optional<Reparacion> findByNumeroTicket(String numeroTicket);

    List<Reparacion> findByCliente_CedulaOrderByFechaCreacionDesc(String clienteCedula);

    List<Reparacion> findByTecnicoAsignado_CedulaOrderByFechaCreacionDesc(String tecnicoCedula);

    List<Reparacion> findByEstado(EstadoReparacion estado);

    List<Reparacion> findAllByOrderByFechaCreacionDesc();

    Optional<Reparacion> findByIdAndCliente_Cedula(Integer id, String clienteCedula);

    boolean existsByCliente_CedulaAndEstadoNotIn(String clienteCedula, Collection<EstadoReparacion> estados);
}
