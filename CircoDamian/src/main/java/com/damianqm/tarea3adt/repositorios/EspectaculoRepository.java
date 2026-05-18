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
}
