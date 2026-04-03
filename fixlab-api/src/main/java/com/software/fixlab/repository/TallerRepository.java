package com.software.fixlab.repository;

import com.software.fixlab.entity.Taller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TallerRepository extends JpaRepository<Taller, Integer> {

    List<Taller> findByTipoTaller_Id(Integer tipoTallerId);
}
