package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio de espectáculos y números (CU5). Se encarga de toda la lógica:
 * crear, modificar y eliminar espectáculos y números, y asignar artistas.
 */
@Service
public class EspectaculoService {

	@Autowired
	private EspectaculoRepository espectaculoRepository;
	@Autowired
	private EspectaculoNumeroRepository enRepository;
	@Autowired
	private NumeroRepository numeroRepository;
	@Autowired
	private CoordinacionRepository coordinacionRepository;
	@Autowired
	private ArtistaRepository artistaRepository;


	@Transactional(readOnly = true)
	public List<Espectaculo> findAll() {
		return espectaculoRepository.findAllOrdenados();
	}

	@Transactional(readOnly = true)
	public List<Numero> findAllNumeros() {
		return numeroRepository.findAllConArtistas();
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
	public Optional<Numero> findNumeroByIdConArtistas(Long id) {
		return numeroRepository.findByIdConArtistas(id);
	}

	/**
	 * Carga un espectáculo con sus números y los artistas de cada número. Se hace
	 * en consultas separadas para no tener duplicados al usar JOIN FETCH.
	 */
	@Transactional(readOnly = true)
	public Optional<Espectaculo> findByIdCompleto(Long id) {
		Optional<Espectaculo> opt = espectaculoRepository.findById(id);
		if (opt.isPresent()) {
			Espectaculo esp = opt.get();
			List<EspectaculoNumero> relaciones = enRepository.findByEspectaculoIdOrderByOrdenAsc(esp.getId());
			for (EspectaculoNumero en : relaciones) {
				Optional<Numero> nConArts = numeroRepository.findByIdConArtistas(en.getNumero().getId());
				if (nConArts.isPresent()) {
					en.getNumero().setArtistas(nConArts.get().getArtistas());
				}
			}
			esp.setNumerosEnEspectaculo(relaciones);
		}
		return opt;
	}

	// ─── Espectáculo (CU5A) ───────────────────────────────────────────

	/** Valida datos del Paso 1 sin guardar nada. */
	public void validarDatosEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idCoord, Long idExcluir) {
		validarEspectaculo(nombre, inicio, fin, idExcluir);
		if (idCoord == null) {
			throw new IllegalArgumentException("Selecciona un coordinador.");
		}
		if (!coordinacionRepository.existsById(idCoord)) {
			throw new IllegalArgumentException("Coordinador no encontrado.");
		}
	}

	/**
	 * Crea o modifica un espectáculo con sus números. Si idExistente es null crea
	 * uno nuevo, si tiene valor modifica el existente.
	 */
	@Transactional
	public Espectaculo persistirEspectaculoCompleto(String nombre, LocalDate inicio, LocalDate fin, Long idCoord,
			List<NumeroAsignado> numeros, Long idExistente) {
		validarEspectaculo(nombre, inicio, fin, idExistente);

		Coordinacion coord = coordinacionRepository.findById(idCoord)
				.orElseThrow(() -> new IllegalArgumentException("Coordinador no encontrado."));

		if (numeros == null || numeros.size() < 3) {
			throw new IllegalArgumentException("El espectáculo necesita al menos 3 números.");
		}

		// Comprobar que no se repitan órdenes
		Set<Integer> ordenes = new HashSet<>();
		for (NumeroAsignado na : numeros) {
			if (na.getOrden() <= 0) {
				throw new IllegalArgumentException("El orden debe ser mayor o igual a 1.");
			}
			if (!ordenes.add(na.getOrden())) {
				throw new IllegalArgumentException("Hay dos números con el orden " + na.getOrden() + ".");
			}
			if (!numeroRepository.existsById(na.getIdNumero())) {
				throw new IllegalArgumentException("El número con id " + na.getIdNumero() + " no existe.");
			}
		}

		Espectaculo esp;
		if (idExistente != null) {
			// Modificación: cargar, vaciar los números existentes y actualizar datos
			esp = espectaculoRepository.findById(idExistente)
					.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
			esp.getNumerosEnEspectaculo().clear();
			esp.setNombre(nombre.trim());
			esp.setFechaInicio(inicio);
			esp.setFechaFin(fin);
			esp.setCoordinador(coord);
			// Con saveAndFlush forzamos que el clear se aplique antes de los insert
			esp = espectaculoRepository.saveAndFlush(esp);
		} else {
			esp = new Espectaculo(nombre.trim(), inicio, fin, coord);
			esp = espectaculoRepository.save(esp);
		}

		// Insertar las filas de espectaculo_numero
		for (NumeroAsignado na : numeros) {
			Numero n = numeroRepository.findById(na.getIdNumero()).get();
			enRepository.save(new EspectaculoNumero(esp, n, na.getOrden()));
		}
		return esp;
	}

	@Transactional
	public void eliminarEspectaculo(Long id) {
		Espectaculo esp = espectaculoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
		espectaculoRepository.delete(esp);
	}

	// ─── Números (CU5B) ───────────────────────────────────────────────

	@Transactional
	public Numero crearNumero(String nombre, double duracion, Set<Long> idsArtistas) {
		validarNumero(nombre, duracion, idsArtistas);
		if (numeroRepository.existsByNombre(nombre.trim())) {
			throw new IllegalArgumentException("Ya existe un número con ese nombre.");
		}

		Numero n = new Numero(nombre.trim(), duracion);
		n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
		return numeroRepository.save(n);
	}

	@Transactional
	public Numero modificarNumero(Long id, String nombre, double duracion, Set<Long> idsArtistas) {
		validarNumero(nombre, duracion, idsArtistas);
		Numero n = numeroRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));

		// Solo comprobar unicidad si el nombre cambia
		if (!n.getNombre().equalsIgnoreCase(nombre.trim()) && numeroRepository.existsByNombre(nombre.trim())) {
			throw new IllegalArgumentException("Ya existe un número con ese nombre.");
		}

		n.setNombre(nombre.trim());
		n.setDuracion(duracion);
		n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
		return numeroRepository.save(n);
	}

	@Transactional
	public void eliminarNumero(Long id) {
		Numero n = numeroRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));
		if (enRepository.countByNumeroId(id) > 0) {
			throw new IllegalArgumentException("El número '" + n.getNombre() + "' está asignado a algún espectáculo. "
					+ "Quítalo primero de los espectáculos.");
		}
		numeroRepository.delete(n);
	}

	// ─── DTO: número con su orden dentro del espectáculo ──────────────

	/**
	 * Clase simple para pasar los números y su orden desde el controlador al
	 * servicio. No es una entidad, solo sirve para agrupar los datos.
	 */
	public static class NumeroAsignado {
		private final Long idNumero;
		private final int orden;

		public NumeroAsignado(Long idNumero, int orden) {
			this.idNumero = idNumero;
			this.orden = orden;
		}

		public Long getIdNumero() {
			return idNumero;
		}

		public int getOrden() {
			return orden;
		}
	}

	// ─── Validaciones ─────────────────────────────────────────────────

	private void validarEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idExcluir) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del espectáculo es obligatorio.");
		}
		if (nombre.trim().length() > 25) {
			throw new IllegalArgumentException("El nombre no puede superar 25 caracteres.");
		}
		// Comprobar unicidad (dejando pasar el propio espectáculo si se modifica)
		Optional<Espectaculo> ex = espectaculoRepository.findByNombre(nombre.trim());
		if (ex.isPresent() && !ex.get().getId().equals(idExcluir)) {
			throw new IllegalArgumentException("Ya existe un espectáculo con ese nombre.");
		}
		if (inicio == null || fin == null) {
			throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
		}
		if (!fin.isAfter(inicio)) {
			throw new IllegalArgumentException("La fecha fin debe ser posterior a la de inicio.");
		}
		if (ChronoUnit.DAYS.between(inicio, fin) > 365) {
			throw new IllegalArgumentException("El periodo no puede superar 1 año.");
		}
	}

	private void validarNumero(String nombre, double duracion, Set<Long> idsArtistas) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del número es obligatorio.");
		}
		if (nombre.trim().length() > 100) {
			throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
		}
		if (duracion <= 0) {
			throw new IllegalArgumentException("La duración debe ser positiva.");
		}
		// Solo admite parte decimal .0 o .5 (con una pequeña tolerancia)
		double dec = duracion - Math.floor(duracion);
		if (Math.abs(dec) > 0.01 && Math.abs(dec - 0.5) > 0.01) {
			throw new IllegalArgumentException("La duración solo admite x,0 o x,5 (ej: 3,0 o 2,5).");
		}
		if (idsArtistas == null || idsArtistas.isEmpty()) {
			throw new IllegalArgumentException("El número debe tener al menos 1 artista.");
		}
	}
}
