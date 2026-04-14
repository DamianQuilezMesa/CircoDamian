package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Numero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NumeroRepository extends JpaRepository<Numero, Long> {

    /** Todos los números con artistas cargados — sin duplicados */
    @Query("SELECT DISTINCT n FROM Numero n LEFT JOIN FETCH n.artistas ORDER BY n.nombre ASC")
    List<Numero> findAllConArtistas();

    /** Un número concreto con artistas */
    @Query("SELECT DISTINCT n FROM Numero n LEFT JOIN FETCH n.artistas WHERE n.id = :id")
    Optional<Numero> findByIdConArtistas(@Param("id") Long id);

    Optional<Numero> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
