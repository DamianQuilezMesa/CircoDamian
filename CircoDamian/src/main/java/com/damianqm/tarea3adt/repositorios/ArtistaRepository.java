package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

    List<Artista> findByEspecialidadesContaining(Especialidad especialidad);

    /**
     * Carga el artista con sus artistas y especialidades.
     * La trayectoria (espectáculos) se carga desde EspectaculoNumero en el servicio.
     */
    @Query("SELECT DISTINCT a FROM Artista a " +
           "LEFT JOIN FETCH a.numeros n " +
           "WHERE a.id = :id")
    Optional<Artista> findByIdConNumeros(@Param("id") Long id);
}
