package com.damianqm.tarea3adt.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.damianqm.tarea3adt.modelo.Numero;

@Repository
public interface NumeroRepository extends JpaRepository<Numero, Long> {

	/** Todos los números de un espectáculo ordenados por posición. */
	List<Numero> findByEspectaculoIdOrderByOrdenAsc(Long idEspectaculo);

	/** Un número concreto con sus artistas ya cargados. */
	@Query("SELECT DISTINCT n FROM Numero n LEFT JOIN FETCH n.artistas WHERE n.id = :id")
	Optional<Numero> findByIdConArtistas(@Param("id") Long id);

	/**
	 * Todos los números de un espectáculo con artistas cargados. Se usa DISTINCT
	 * para evitar duplicados por el JOIN.
	 */
	@Query("SELECT DISTINCT n FROM Numero n LEFT JOIN FETCH n.artistas "
			+ "WHERE n.espectaculo.id = :idEsp ORDER BY n.orden ASC")
	List<Numero> findByEspectaculoIdConArtistas(@Param("idEsp") Long idEspectaculo);

	/** Comprueba si ya existe un orden concreto dentro de un espectáculo. */
	boolean existsByEspectaculoIdAndOrden(Long idEspectaculo, int orden);

	/** Comprueba orden excluyendo un número concreto (útil al modificar). */
	@Query("SELECT COUNT(n) > 0 FROM Numero n "
			+ "WHERE n.espectaculo.id = :idEsp AND n.orden = :orden AND n.id <> :idNum")
	boolean existsByEspectaculoIdAndOrdenExcluyendo(@Param("idEsp") Long idEsp, @Param("orden") int orden,
			@Param("idNum") Long idNum);

	/** Máximo orden actual en el espectáculo (para sugerir el siguiente). */
	@Query("SELECT COALESCE(MAX(n.orden), 0) FROM Numero n WHERE n.espectaculo.id = :idEsp")
	int maxOrdenEnEspectaculo(@Param("idEsp") Long idEspectaculo);
}
