package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.EspectaculoNumero;
import com.damianqm.tarea3adt.modelo.EspectaculoNumeroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EspectaculoNumeroRepository extends JpaRepository<EspectaculoNumero, EspectaculoNumeroId> {

    List<EspectaculoNumero> findByEspectaculoIdOrderByOrdenAsc(Long idEspectaculo);

    /** Cuenta en cuántos espectáculos está un número (para validar borrado). */
    int countByNumeroId(Long idNumero);

    /** Apariciones de un número en todos los espectáculos, con el espectáculo cargado. */
    @Query("SELECT en FROM EspectaculoNumero en JOIN FETCH en.espectaculo " +
           "WHERE en.numero.id = :idNumero ORDER BY en.espectaculo.nombre ASC")
    List<EspectaculoNumero> findByNumeroId(@Param("idNumero") Long idNumero);
}
