package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Coordinacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinacionRepository extends JpaRepository<Coordinacion, Long> {
    List<Coordinacion> findBySeniorTrue();
}
