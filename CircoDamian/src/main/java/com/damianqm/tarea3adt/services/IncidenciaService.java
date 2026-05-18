package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.objectdb.Incidencia;
import com.damianqm.tarea3adt.modelo.objectdb.ResolucionIncidencia;
import com.damianqm.tarea3adt.modelo.objectdb.TipoIncidencia;
import com.damianqm.tarea3adt.repositorios.objectdb.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IncidenciaService {

	@Autowired
	private IncidenciaRepository incidenciaRepository;

	@Autowired
	private SesionService sesionService;

	// CU8 – Registrar nueva incidencia
	public Incidencia registrar(TipoIncidencia tipo, String descripcion, Long idEspectaculo, Long idNumero) {

		if (tipo == null)
			throw new IllegalArgumentException("El tipo de incidencia es obligatorio.");
		if (descripcion == null || descripcion.isBlank())
			throw new IllegalArgumentException("La descripción es obligatoria.");
		if (descripcion.length() > 1000)
			throw new IllegalArgumentException("La descripción no puede superar 1000 caracteres.");

		Long idPersona = sesionService.getIdPersonaActual();
		Incidencia inc = new Incidencia(tipo, descripcion.trim(), idPersona, idEspectaculo, idNumero);
		return incidenciaRepository.guardar(inc);
	}

	// CU9 – Resolver incidencia (solo Coordinación y Administrador)
	public void resolver(Long idIncidencia, String accionesRealizadas) {
		// PT4-12: acceso restringido
		if (!sesionService.isCoordinacion()) {
			throw new IllegalStateException(
					"Acceso denegado. Solo Coordinación o Administrador pueden resolver incidencias.");
		}
		if (accionesRealizadas == null || accionesRealizadas.isBlank())
			throw new IllegalArgumentException("Describe las acciones realizadas para resolver la incidencia.");

		Long idPersona = sesionService.getIdPersonaActual();
		ResolucionIncidencia resolucion = new ResolucionIncidencia(accionesRealizadas.trim(), idPersona, null);

		incidenciaRepository.resolver(idIncidencia, resolucion);
	}

	// CU11 – Consultar incidencias con filtros
	public List<Incidencia> consultar(TipoIncidencia tipo, Boolean resuelta, Long idEspectaculo, Long idNumero,
			LocalDateTime desde, LocalDateTime hasta) {
		return incidenciaRepository.consultar(tipo, resuelta, idEspectaculo, idNumero, desde, hasta);
	}

	public List<Incidencia> findAll() {
		return incidenciaRepository.findAll();
	}

	public Optional<Incidencia> findById(Long id) {
		return incidenciaRepository.findById(id);
	}
}
