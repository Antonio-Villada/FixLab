package com.software.fixlab.repository;

import com.software.fixlab.entity.SolicitudPqr;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudPqrRepository extends JpaRepository<SolicitudPqr, Long> {

    @EntityGraph(attributePaths = {"pedido", "reparacion", "tecnicoValidacion"})
    List<SolicitudPqr> findByCliente_CedulaOrderByFechaRadicacionDesc(String clienteCedula);

    @EntityGraph(attributePaths = {"cliente", "pedido", "reparacion", "tecnicoValidacion"})
    List<SolicitudPqr> findAllByOrderByFechaRadicacionDesc();

    @Query("SELECT s FROM SolicitudPqr s LEFT JOIN FETCH s.cliente LEFT JOIN FETCH s.pedido "
            + "LEFT JOIN FETCH s.reparacion LEFT JOIN FETCH s.tecnicoValidacion WHERE s.id = :id")
    Optional<SolicitudPqr> findDetailedById(@Param("id") Long id);

    @Query("SELECT s FROM SolicitudPqr s LEFT JOIN FETCH s.cliente LEFT JOIN FETCH s.pedido "
            + "LEFT JOIN FETCH s.reparacion LEFT JOIN FETCH s.tecnicoValidacion "
            + "WHERE s.id = :id AND s.cliente.cedula = :cedula")
    Optional<SolicitudPqr> findDetailedByIdAndCliente_Cedula(@Param("id") Long id, @Param("cedula") String cedula);

    Optional<SolicitudPqr> findByIdAndCliente_Cedula(Long id, String clienteCedula);
}
