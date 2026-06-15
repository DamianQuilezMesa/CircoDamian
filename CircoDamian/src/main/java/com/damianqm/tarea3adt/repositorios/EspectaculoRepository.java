package com.damianqm.tarea3adt.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.damianqm.tarea3adt.modelo.Espectaculo;

@Repository
public interface EspectaculoRepository extends JpaRepository<Espectaculo, Long> {

	Optional<Espectaculo> findByNombre(String nombre);

	@Query("SELECT e FROM Espectaculo e ORDER BY e.id ASC")
	List<Espectaculo> findAllOrdenados();

	/** Ids de los espectáculos dirigidos por un coordinador concreto. */
	@Query("SELECT e.id FROM Espectaculo e WHERE e.coordinador.id = :idCoord")
	List<Long> findIdsByCoordinador(@org.springframework.data.repository.query.Param("idCoord") Long idCoord);

	/**
	 * Ids de los espectáculos en los que participa un artista (en alguno de sus
	 * números).
	 */
	@Query("SELECT DISTINCT n.espectaculo.id FROM Numero n JOIN n.artistas a WHERE a.id = :idArtista")
	List<Long> findIdsByArtistaParticipante(
			@org.springframework.data.repository.query.Param("idArtista") Long idArtista);
}
