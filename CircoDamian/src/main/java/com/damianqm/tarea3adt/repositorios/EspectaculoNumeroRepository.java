package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.EspectaculoNumero;
import com.damianqm.tarea3adt.modelo.EspectaculoNumeroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspectaculoNumeroRepository
        extends JpaRepository<EspectaculoNumero, EspectaculoNumeroId> {

    /**
     * Números de un espectáculo ordenados por orden, con Numero cargado (EAGER en la entidad).
     * No se hace FETCH de artistas aquí para evitar duplicados — se cargan por separado.
     */
    List<EspectaculoNumero> findByEspectaculoIdOrderByOrdenAsc(Long idEspectaculo);

    int countByEspectaculoId(Long idEspectaculo);
    boolean existsByEspectaculoIdAndOrden(Long idEspectaculo, int orden);
    boolean existsByEspectaculoIdAndNumeroId(Long idEspectaculo, Long idNumero);
    void deleteByEspectaculoId(Long idEspectaculo);
    int countByNumeroId(Long idNumero);

    /**
     * Espectáculos donde aparece un número, con Espectaculo cargado.
     */
    @Query("SELECT en FROM EspectaculoNumero en " +
           "JOIN FETCH en.espectaculo " +
           "WHERE en.numero.id = :idNumero " +
           "ORDER BY en.espectaculo.nombre ASC")
    List<EspectaculoNumero> findByNumeroId(@Param("idNumero") Long idNumero);
}
