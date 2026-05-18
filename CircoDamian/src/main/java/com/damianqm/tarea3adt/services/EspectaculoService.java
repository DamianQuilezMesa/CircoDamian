package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import com.damianqm.tarea3adt.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EspectaculoService {

	@Autowired
	private EspectaculoRepository espectaculoRepository;
	@Autowired
	private NumeroRepository numeroRepository;
	@Autowired
	private CoordinacionRepository coordinacionRepository;
	@Autowired
	private ArtistaRepository artistaRepository;
	@Autowired
	private SesionService sesionService;
	@Autowired
	private LogService logService;

	@Transactional(readOnly = true)
	public List<Espectaculo> findAll() {
		return espectaculoRepository.findAllOrdenados();
	}

	@Transactional(readOnly = true)
	public List<Coordinacion> findAllCoordinadores() {
		return coordinacionRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Artista> findAllArtistas() {
		return artistaRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<Espectaculo> findByIdCompleto(Long id) {
		Optional<Espectaculo> opt = espectaculoRepository.findById(id);
		if (opt.isPresent()) {
			Espectaculo esp = opt.get();
			List<Numero> numeros = numeroRepository.findByEspectaculoIdConArtistas(esp.getId());
			esp.setNumeros(numeros);
		}
		return opt;
	}

	@Transactional(readOnly = true)
	public List<Numero> findNumerosPorEspectaculo(Long idEspectaculo) {
		return numeroRepository.findByEspectaculoIdConArtistas(idEspectaculo);
	}

	@Transactional(readOnly = true)
	public Optional<Numero> findNumeroByIdConArtistas(Long id) {
		return numeroRepository.findByIdConArtistas(id);
	}

	@Transactional(readOnly = true)
	public int siguienteOrden(Long idEspectaculo) {
		return numeroRepository.maxOrdenEnEspectaculo(idEspectaculo) + 1;
	}

	public void validarDatosEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idCoord, Long idExcluir) {
		validarEspectaculo(nombre, inicio, fin, idExcluir);
		if (idCoord == null)
			throw new IllegalArgumentException("Selecciona un coordinador.");
		if (!coordinacionRepository.existsById(idCoord))
			throw new IllegalArgumentException("Coordinador no encontrado.");
	}

	@Transactional
	public Espectaculo persistirEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idCoord,
			Long idExistente) {
		validarEspectaculo(nombre, inicio, fin, idExistente);

		Coordinacion coord = coordinacionRepository.findById(idCoord)
				.orElseThrow(() -> new IllegalArgumentException("Coordinador no encontrado."));

		if (idExistente != null) {
			Espectaculo esp = espectaculoRepository.findById(idExistente)
					.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
			esp.setNombre(nombre.trim());
			esp.setFechaInicio(inicio);
			esp.setFechaFin(fin);
			esp.setCoordinador(coord);
			Espectaculo saved = espectaculoRepository.save(esp);
			logService.registrarOperacion(sesionService.getNombreUsuarioActual(), TipoOperacion.ACTUALIZACION,
					"Se ha actualizado la informacion del id " + saved.getId() + " de Espectaculo");
			return saved;
		} else {
			Espectaculo saved = espectaculoRepository.save(new Espectaculo(nombre.trim(), inicio, fin, coord));
			logService.registrarOperacion(sesionService.getNombreUsuarioActual(), TipoOperacion.NUEVO,
					"Se ha insertado un nuevo Espectaculo de id " + saved.getId());
			return saved;
		}
	}

	@Transactional
	public Numero crearNumero(Long idEspectaculo, String nombre, double duracion, int orden, Set<Long> idsArtistas) {
		Espectaculo esp = espectaculoRepository.findById(idEspectaculo)
				.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));

		validarNumero(nombre, duracion, idsArtistas);
		validarOrden(idEspectaculo, orden, null);

		Numero n = new Numero(nombre.trim(), duracion, orden, esp);
		n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
		Numero saved = numeroRepository.save(n);
		logService.registrarOperacion(sesionService.getNombreUsuarioActual(), TipoOperacion.NUEVO,
				"Se ha insertado un nuevo Numero de id " + saved.getId());
		return saved;
	}

	@Transactional
	public Numero modificarNumero(Long idNumero, String nombre, double duracion, int orden, Set<Long> idsArtistas) {
		Numero n = numeroRepository.findById(idNumero)
				.orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));

		validarNumero(nombre, duracion, idsArtistas);
		if (n.getOrden() != orden)
			validarOrden(n.getEspectaculo().getId(), orden, idNumero);

		n.setNombre(nombre.trim());
		n.setDuracion(duracion);
		n.setOrden(orden);
		n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
		Numero saved = numeroRepository.save(n);
		logService.registrarOperacion(sesionService.getNombreUsuarioActual(), TipoOperacion.ACTUALIZACION,
				"Se ha actualizado la informacion del id " + saved.getId() + " de Numero");
		return saved;
	}

	@Transactional
	public void eliminarNumero(Long idNumero) {
		Numero n = numeroRepository.findById(idNumero)
				.orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));
		long total = numeroRepository.findByEspectaculoIdOrderByOrdenAsc(n.getEspectaculo().getId()).size();
		if (total <= 3)
			throw new IllegalArgumentException("No se puede eliminar: el espectáculo necesita al menos 3 números.");
		logService.registrarOperacion(sesionService.getNombreUsuarioActual(), TipoOperacion.BORRADO,
				"Se ha borrado el Numero de id " + idNumero);
		numeroRepository.delete(n);
	}

	private void validarEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idExcluir) {
		if (nombre == null || nombre.isBlank())
			throw new IllegalArgumentException("El nombre del espectáculo es obligatorio.");
		if (nombre.trim().length() > 25)
			throw new IllegalArgumentException("El nombre no puede superar 25 caracteres.");
		Optional<Espectaculo> ex = espectaculoRepository.findByNombre(nombre.trim());
		if (ex.isPresent() && !ex.get().getId().equals(idExcluir))
			throw new IllegalArgumentException("Ya existe un espectáculo con ese nombre.");
		if (inicio == null || fin == null)
			throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
		if (!fin.isAfter(inicio))
			throw new IllegalArgumentException("La fecha fin debe ser posterior a la de inicio.");
		if (ChronoUnit.DAYS.between(inicio, fin) > 365)
			throw new IllegalArgumentException("El periodo no puede superar 1 año.");
	}

	private void validarNumero(String nombre, double duracion, Set<Long> idsArtistas) {
		if (nombre == null || nombre.isBlank())
			throw new IllegalArgumentException("El nombre del número es obligatorio.");
		if (nombre.trim().length() > 100)
			throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
		if (duracion <= 0)
			throw new IllegalArgumentException("La duración debe ser positiva.");
		double dec = duracion - Math.floor(duracion);
		if (Math.abs(dec) > 0.01 && Math.abs(dec - 0.5) > 0.01)
			throw new IllegalArgumentException("La duración solo admite x,0 o x,5 (ej: 3,0 o 2,5).");
		if (idsArtistas == null || idsArtistas.isEmpty())
			throw new IllegalArgumentException("El número debe tener al menos 1 artista.");
	}

	private void validarOrden(Long idEspectaculo, int orden, Long idNumeroExcluir) {
		if (orden <= 0)
			throw new IllegalArgumentException("El orden debe ser mayor o igual a 1.");
		boolean colision = (idNumeroExcluir == null)
				? numeroRepository.existsByEspectaculoIdAndOrden(idEspectaculo, orden)
				: numeroRepository.existsByEspectaculoIdAndOrdenExcluyendo(idEspectaculo, orden, idNumeroExcluir);
		if (colision)
			throw new IllegalArgumentException("Ya existe un número con el orden " + orden + " en este espectáculo.");
	}
}
