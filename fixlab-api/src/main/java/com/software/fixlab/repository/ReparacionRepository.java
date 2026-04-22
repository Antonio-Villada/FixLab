package com.software.fixlab.repository;

import com.software.fixlab.entity.EstadoReparacion;
import com.software.fixlab.entity.Reparacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<Reparacion> findByFechaCreacionGreaterThanEqualAndFechaCreacionBefore(LocalDateTime d1, LocalDateTime d2);

    long countByFechaCreacionGreaterThanEqualAndFechaCreacionBefore(LocalDateTime d1, LocalDateTime d2);

    @Query("SELECT r.estado, COUNT(r) FROM Reparacion r WHERE r.fechaCreacion >= :d1 AND r.fechaCreacion < :d2 GROUP BY r.estado")
    List<Object[]> contarPorEstadoEnPeriodo(@Param("d1") LocalDateTime d1, @Param("d2") LocalDateTime d2);

    List<Reparacion> findByEstadoAndFechaFinGarantiaServicioIsNotNullAndFechaFinGarantiaServicioBefore(
            EstadoReparacion estado,
            LocalDate fechaExclusiva);

    List<Reparacion> findByEstadoAndFechaFinGarantiaServicioBetween(
            EstadoReparacion estado,
            LocalDate desdeInclusive,
            LocalDate hastaInclusive);

    @Query("SELECT t.cedula, CONCAT(CONCAT(t.nombre, ' '), t.apellido), "
            + "SUM(CASE WHEN r.estado = :ent THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN r.estado <> :ent AND r.estado <> :can THEN 1 ELSE 0 END) "
            + "FROM Reparacion r JOIN r.tecnicoAsignado t "
            + "WHERE r.fechaCreacion >= :d1 AND r.fechaCreacion < :d2 "
            + "GROUP BY t.cedula, t.nombre, t.apellido "
            + "ORDER BY SUM(CASE WHEN r.estado = :ent THEN 1 ELSE 0 END) DESC")
    List<Object[]> resumenPorTecnico(
            @Param("d1") LocalDateTime d1,
            @Param("d2") LocalDateTime d2,
            @Param("ent") EstadoReparacion entregado,
            @Param("can") EstadoReparacion cancelado);
}
