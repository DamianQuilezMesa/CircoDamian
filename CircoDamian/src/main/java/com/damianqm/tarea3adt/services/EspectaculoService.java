package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.dto.EspectaculoBorrador;
import com.damianqm.tarea3adt.dto.NumeroBorrador;
import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import com.damianqm.tarea3adt.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
	@Autowired
	private DossierService dossierService;
	@Autowired
	private InformeXmlService informeXmlService;

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

	/** Todos los números del circo, independientemente del espectáculo. */
	@Transactional(readOnly = true)
	public List<Numero> findAllNumeros() {
		return numeroRepository.findAll();
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

			boolean sinCambios = esp.getNombre().equals(nombre.trim()) && esp.getFechaInicio().equals(inicio)
					&& esp.getFechaFin().equals(fin) && esp.getCoordinador().getId().equals(idCoord);
			if (sinCambios)
				return esp;

			esp.setNombre(nombre.trim());
			esp.setFechaInicio(inicio);
			esp.setFechaFin(fin);
			esp.setCoordinador(coord);
			Espectaculo saved = espectaculoRepository.save(esp);
			logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.ACTUALIZACION,
					"Modificación de Espectáculo [id=" + saved.getId() + "] " + saved.getNombre());
			regenerarInformeXmlSiExiste(saved.getId());
			return saved;
		} else {
			Espectaculo saved = espectaculoRepository.save(new Espectaculo(nombre.trim(), inicio, fin, coord));
			logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.NUEVO,
					"Nuevo Espectáculo [id=" + saved.getId() + "] " + saved.getNombre());
			return saved;
		}
	}

	// ============================================================
	// NUEVO FLUJO: el espectáculo y sus números se montan en memoria
	// (EspectaculoBorrador) y se persisten todos juntos al confirmar,
	// validando el conjunto completo en una sola transacción.
	// ============================================================

	/**
	 * Carga un espectáculo ya existente en un borrador en memoria para poder
	 * editarlo (datos básicos + números + artistas) sin tocar la BD hasta guardar.
	 */
	@Transactional(readOnly = true)
	public EspectaculoBorrador cargarBorrador(Long idEspectaculo) {
		Espectaculo esp = espectaculoRepository.findById(idEspectaculo)
				.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));

		EspectaculoBorrador b = new EspectaculoBorrador();
		b.setId(esp.getId());
		b.setNombre(esp.getNombre());
		b.setFechaInicio(esp.getFechaInicio());
		b.setFechaFin(esp.getFechaFin());
		b.setCoordinador(esp.getCoordinador());

		List<Numero> numeros = numeroRepository.findByEspectaculoIdConArtistas(idEspectaculo);
		for (Numero n : numeros) {
			b.getNumeros().add(new NumeroBorrador(n.getId(), n.getNombre(), n.getDuracion(), n.getOrden(),
					new HashSet<>(n.getArtistas())));
		}
		return b;
	}

	/**
	 * Valida y persiste un borrador completo (espectáculo + todos sus números) en
	 * una sola transacción. Si algo no cumple, lanza excepción y NO se persiste
	 * nada. Sirve tanto para alta como para modificación.
	 */
	@Transactional
	public Espectaculo guardarEspectaculoCompleto(EspectaculoBorrador b) {
		// 1) Validar datos básicos del espectáculo
		Long idCoord = b.getCoordinador() != null ? b.getCoordinador().getId() : null;
		validarEspectaculo(b.getNombre(), b.getFechaInicio(), b.getFechaFin(), b.getId());
		if (idCoord == null)
			throw new IllegalArgumentException("Selecciona un coordinador.");
		Coordinacion coord = coordinacionRepository.findById(idCoord)
				.orElseThrow(() -> new IllegalArgumentException("Coordinador no encontrado."));

		// 2) Validar el conjunto de números
		validarConjuntoNumeros(b.getNumeros());

		boolean esNuevo = b.esNuevo();
		Espectaculo esp;

		if (esNuevo) {
			esp = new Espectaculo(b.getNombre().trim(), b.getFechaInicio(), b.getFechaFin(), coord);
		} else {
			esp = espectaculoRepository.findById(b.getId())
					.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
			esp.setNombre(b.getNombre().trim());
			esp.setFechaInicio(b.getFechaInicio());
			esp.setFechaFin(b.getFechaFin());
			esp.setCoordinador(coord);
		}

		// 3) Reconstruir la lista de números sobre la entidad gestionada.
		// Pre-cargamos TODOS los artistas necesarios en UNA sola consulta ANTES de
		// tocar la colección. Si la consulta se hace a mitad del bucle (como antes),
		// JPA dispara un auto-flush con la colección a medio reconstruir e intenta
		// insertar números nuevos antes de borrar los antiguos -> choca con la
		// restricción única (id_espectaculo, orden).
		Set<Long> idsArtistas = b.getNumeros().stream().flatMap(nb -> nb.getArtistas().stream()).map(Artista::getId)
				.collect(Collectors.toSet());
		Map<Long, Artista> artistasPorId = artistaRepository.findAllById(idsArtistas).stream()
				.collect(Collectors.toMap(Artista::getId, a -> a));

		// Para poder registrar un log por cada número creado o eliminado en este
		// flujo, capturamos los números que YA existían (id -> nombre) y el conjunto
		// de ids que llegan en el borrador. Un número del borrador sin id es nuevo;
		// un id que existía y ya no viene en el borrador es una baja.
		Map<Long, String> numerosAntes = new HashMap<>();
		if (!esNuevo) {
			for (Numero n : numeroRepository.findByEspectaculoIdOrderByOrdenAsc(esp.getId()))
				numerosAntes.put(n.getId(), n.getNombre());
		}
		Set<Long> idsEnBorrador = b.getNumeros().stream().map(NumeroBorrador::getId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		esp.getNumeros().clear();
		if (!esNuevo) {
			// Forzamos el BORRADO de los números antiguos antes de insertar los nuevos,
			// para no violar la restricción única (id_espectaculo, orden).
			espectaculoRepository.flush();
		}
		for (NumeroBorrador nb : b.getNumeros()) {
			Numero n = new Numero(nb.getNombre().trim(), nb.getDuracion(), nb.getOrden(), esp);
			Set<Artista> arts = nb.getArtistas().stream().map(a -> artistasPorId.get(a.getId()))
					.filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
			n.setArtistas(arts);
			esp.getNumeros().add(n);
		}

		Espectaculo guardado = espectaculoRepository.save(esp);

		// 4) Logs DB4O: del espectáculo y de cada número creado/eliminado
		if (esNuevo) {
			logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.NUEVO,
					"Nuevo Espectáculo [id=" + guardado.getId() + "] " + guardado.getNombre());
		} else {
			logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.ACTUALIZACION,
					"Modificación de Espectáculo [id=" + guardado.getId() + "] " + guardado.getNombre());
		}

		// El orden es único por espectáculo: lo usamos para recuperar el id real con
		// el que se ha persistido cada número ya guardado.
		Map<Integer, Long> idPorOrden = new HashMap<>();
		for (Numero n : guardado.getNumeros())
			idPorOrden.put(n.getOrden(), n.getId());

		// Log NUEVO por cada número recién creado (los del borrador sin id previo).
		for (NumeroBorrador nb : b.getNumeros()) {
			if (nb.getId() == null) {
				Long idNum = idPorOrden.get(nb.getOrden());
				logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.NUEVO, "Nuevo Número [id="
						+ idNum + "] " + nb.getNombre().trim() + " (espectáculo " + guardado.getNombre() + ")");
			}
		}

		// Log BORRADO por cada número que existía y ya no está en el borrador.
		for (Map.Entry<Long, String> ant : numerosAntes.entrySet()) {
			if (!idsEnBorrador.contains(ant.getKey())) {
				logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.BORRADO,
						"Borrado Número [id=" + ant.getKey() + "] " + ant.getValue() + " (espectáculo "
								+ guardado.getNombre() + ")");
			}
		}

		// 5) Actualizar dossiers MongoDB de los artistas participantes
		for (Numero n : guardado.getNumeros()) {
			for (Artista artista : n.getArtistas()) {
				dossierService.agregarOActualizarTrayectoria(artista.getId(), n);
			}
		}

		// 6) Si ya existía un informe XML de este espectáculo, regenerarlo
		informeXmlService.regenerarSiExiste(guardado);

		return guardado;
	}

	/**
	 * Valida el conjunto completo de números de un espectáculo: mínimo 3, órdenes
	 * únicos y correctos, y cada número válido.
	 */
	public void validarConjuntoNumeros(List<NumeroBorrador> numeros) {
		if (numeros == null || numeros.size() < 3)
			throw new IllegalArgumentException("El espectáculo debe tener al menos 3 números.");

		Set<Integer> ordenesVistos = new HashSet<>();
		for (NumeroBorrador n : numeros) {
			validarNumero(n.getNombre(), n.getDuracion(),
					n.getArtistas().stream().map(Artista::getId).collect(Collectors.toSet()));
			if (n.getOrden() <= 0)
				throw new IllegalArgumentException("El orden de cada número debe ser mayor o igual a 1.");
			if (!ordenesVistos.add(n.getOrden()))
				throw new IllegalArgumentException("Hay números con el mismo orden (" + n.getOrden()
						+ "). Cada número debe tener un orden único.");
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
		logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.NUEVO,
				"Nuevo Número [id=" + saved.getId() + "] " + saved.getNombre());
		for (Artista artista : saved.getArtistas()) {
			dossierService.agregarOActualizarTrayectoria(artista.getId(), saved);
		}
		regenerarInformeXmlSiExiste(esp.getId());
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
		logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.ACTUALIZACION,
				"Modificación de Número [id=" + saved.getId() + "] " + saved.getNombre());
		for (Artista artista : saved.getArtistas()) {
			dossierService.agregarOActualizarTrayectoria(artista.getId(), saved);
		}
		regenerarInformeXmlSiExiste(saved.getEspectaculo().getId());
		return saved;
	}

	@Transactional
	public void eliminarNumero(Long idNumero) {
		Numero n = numeroRepository.findById(idNumero)
				.orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));
		long total = numeroRepository.findByEspectaculoIdOrderByOrdenAsc(n.getEspectaculo().getId()).size();
		if (total <= 3)
			throw new IllegalArgumentException("No se puede eliminar: el espectáculo necesita al menos 3 números.");
		logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.BORRADO,
				"Borrado Número [id=" + n.getId() + "] " + n.getNombre());
		Long idEspectaculo = n.getEspectaculo().getId();
		numeroRepository.delete(n);
		regenerarInformeXmlSiExiste(idEspectaculo);
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

	/**
	 * Si de este espectáculo ya se había exportado un informe XML alguna vez, lo
	 * regenera para mantenerlo coherente con los cambios (mismo comportamiento que
	 * el proyecto de referencia). Si nunca se exportó, no hace nada. Recarga el
	 * espectáculo con sus números y artistas para generar un informe completo. Es a
	 * prueba de fallos: un eXistDB caído no bloquea la operación.
	 */
	private void regenerarInformeXmlSiExiste(Long idEspectaculo) {
		if (idEspectaculo == null || !informeXmlService.existeXml(idEspectaculo))
			return;
		espectaculoRepository.findById(idEspectaculo).ifPresent(esp -> {
			esp.setNumeros(numeroRepository.findByEspectaculoIdConArtistas(idEspectaculo));
			informeXmlService.regenerarSiExiste(esp);
		});
	}
}
