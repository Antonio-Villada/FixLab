package com.software.fixlab.repository;

import com.software.fixlab.entity.EntradaMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EntradaMercanciaRepository extends JpaRepository<EntradaMercancia, Long> {

    List<EntradaMercancia> findByProducto_IdOrderByFechaRegistroDesc(Long productoId);

    List<EntradaMercancia> findByFechaRegistroGreaterThanEqualAndFechaRegistroBeforeOrderByFechaRegistroDesc(
            Instant desdeInclusive,
            Instant hastaExclusive);
}
