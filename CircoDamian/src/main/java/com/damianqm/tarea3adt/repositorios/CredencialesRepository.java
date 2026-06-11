package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Credenciales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredencialesRepository extends JpaRepository<Credenciales, Long> {
	Optional<Credenciales> findByNombreUsuarioAndPassword(String nombreUsuario, String password);

	Optional<Credenciales> findByNombreUsuario(String nombreUsuario);

	Optional<Credenciales> findByPersonaId(Long idPersona);

	boolean existsByNombreUsuario(String nombreUsuario);
}
