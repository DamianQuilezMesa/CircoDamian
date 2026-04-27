package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {

	/**
	 * Carga el artista con sus números (DISTINCT para evitar duplicados del JOIN).
	 */
	@Query("SELECT DISTINCT a FROM Artista a LEFT JOIN FETCH a.numeros WHERE a.id = :id")
	Optional<Artista> findByIdConNumeros(@Param("id") Long id);
}
