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
			// Forzar la carga de números y artistas sin reemplazar la colección
			// (setNumeros sobre una colección con orphanRemoval puede provocar
			// "collection no longer referenced" al hacer flush).
			esp.getNumeros().forEach(n -> n.getArtistas().size());
		}
		return opt;
	}

	@Transactional(readOnly = true)
	public List<Numero> findNumerosPorEspectaculo(Long idEspectaculo) {
		return numeroRepository.findByEspectaculoIdConArtistas(idEspectaculo);
	}

	/**
	 * Todos los números de todos los espectáculos, ordenados por id. Se usa para
	 * poder elegir un número en las incidencias SIN tener que seleccionar antes su
	 * espectáculo (los selects de número y espectáculo son independientes).
	 */
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
		String login = sesionService.getLoginActual();

		// Pre-cargar todos los artistas necesarios en una sola consulta
		Set<Long> idsArtistas = b.getNumeros().stream().flatMap(nb -> nb.getArtistas().stream()).map(Artista::getId)
				.collect(Collectors.toSet());
		Map<Long, Artista> artistasPorId = artistaRepository.findAllById(idsArtistas).stream()
				.collect(Collectors.toMap(Artista::getId, a -> a));

		if (esNuevo) {
			// ---------- ALTA DE ESPECTÁCULO ----------
			esp = new Espectaculo(b.getNombre().trim(), b.getFechaInicio(), b.getFechaFin(), coord);
			for (NumeroBorrador nb : b.getNumeros()) {
				Numero n = new Numero(nb.getNombre().trim(), nb.getDuracion(), nb.getOrden(), esp);
				n.setArtistas(resolverArtistas(nb, artistasPorId));
				esp.getNumeros().add(n);
			}
			Espectaculo guardado = espectaculoRepository.save(esp);
			espectaculoRepository.flush();

			logService.registrarOperacion(login, TipoOperacion.NUEVO, "Nuevo Espectáculo [id=" + guardado.getId() + "] "
					+ guardado.getNombre() + " con " + guardado.getNumeros().size() + " números");
			for (Numero n : guardado.getNumeros()) {
				logService.registrarOperacion(login, TipoOperacion.NUEVO, "Nuevo Número [id=" + n.getId() + "] "
						+ n.getNombre() + " del Espectáculo [id=" + guardado.getId() + "]");
			}
			refrescarDossiersYInforme(guardado);
			return guardado;
		}

		// ---------- MODIFICACIÓN DE ESPECTÁCULO ----------
		esp = espectaculoRepository.findById(b.getId())
				.orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));

		// ¿Cambió algún dato propio del espectáculo?
		boolean cambioEspectaculo = !esp.getNombre().equals(b.getNombre().trim())
				|| !Objects.equals(esp.getFechaInicio(), b.getFechaInicio())
				|| !Objects.equals(esp.getFechaFin(), b.getFechaFin()) || esp.getCoordinador() == null
				|| !Objects.equals(esp.getCoordinador().getId(), coord.getId());

		esp.setNombre(b.getNombre().trim());
		esp.setFechaInicio(b.getFechaInicio());
		esp.setFechaFin(b.getFechaFin());
		esp.setCoordinador(coord);

		// Mapa de los números que YA existían en BD (por id)
		Map<Long, Numero> existentesPorId = esp.getNumeros().stream().collect(Collectors.toMap(Numero::getId, n -> n));

		// Ids de los números que el borrador conserva (los que tienen id no-nulo)
		Set<Long> idsEnBorrador = b.getNumeros().stream().map(NumeroBorrador::getId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// 3a) BORRADOS: números que estaban y ya no están en el borrador
		List<Numero> aBorrar = esp.getNumeros().stream().filter(n -> !idsEnBorrador.contains(n.getId()))
				.collect(Collectors.toList());
		List<String> logsBorrado = new ArrayList<>();
		for (Numero n : aBorrar) {
			logsBorrado.add("Borrado Número [id=" + n.getId() + "] " + n.getNombre() + " del Espectáculo [id="
					+ esp.getId() + "]");
		}
		esp.getNumeros().removeAll(aBorrar); // orphanRemoval los elimina de la BD
		if (!aBorrar.isEmpty()) {
			// Forzar el DELETE de los números borrados ANTES de insertar/actualizar los
			// demás, para no chocar con la restricción única (id_espectaculo, orden) si
			// algún número nuevo reutiliza un orden que quedó libre.
			espectaculoRepository.flush();
		}

		// 3b) ALTAS y MODIFICACIONES
		List<String> logsAlta = new ArrayList<>();
		List<String> logsModif = new ArrayList<>();
		for (NumeroBorrador nb : b.getNumeros()) {
			Set<Artista> arts = resolverArtistas(nb, artistasPorId);
			if (nb.getId() == null || !existentesPorId.containsKey(nb.getId())) {
				// Número nuevo dentro de un espectáculo existente
				Numero n = new Numero(nb.getNombre().trim(), nb.getDuracion(), nb.getOrden(), esp);
				n.setArtistas(arts);
				esp.getNumeros().add(n);
				logsAlta.add(nb.getNombre().trim()); // el id se conoce tras el flush
			} else {
				// Número existente: actualizar in situ (conserva su id)
				Numero n = existentesPorId.get(nb.getId());
				boolean cambioNumero = !n.getNombre().equals(nb.getNombre().trim())
						|| n.getDuracion() != nb.getDuracion() || n.getOrden() != nb.getOrden()
						|| !mismosArtistas(n.getArtistas(), arts);
				n.setNombre(nb.getNombre().trim());
				n.setDuracion(nb.getDuracion());
				n.setOrden(nb.getOrden());
				n.setArtistas(arts);
				if (cambioNumero)
					logsModif.add("Modificación de Número [id=" + n.getId() + "] " + n.getNombre()
							+ " del Espectáculo [id=" + esp.getId() + "]");
			}
		}

		Espectaculo guardado = espectaculoRepository.save(esp);
		espectaculoRepository.flush();

		// 4) Registrar logs DB4O reflejando EXACTAMENTE lo que ha cambiado
		if (cambioEspectaculo) {
			logService.registrarOperacion(login, TipoOperacion.ACTUALIZACION,
					"Modificación de Espectáculo [id=" + guardado.getId() + "] " + guardado.getNombre());
		}
		// Altas (ahora sí con id asignado): localizamos los números nuevos por
		// nombre+orden
		for (Numero n : guardado.getNumeros()) {
			for (String nombreNuevo : logsAlta) {
				if (n.getNombre().equals(nombreNuevo)) {
					logService.registrarOperacion(login, TipoOperacion.NUEVO, "Nuevo Número [id=" + n.getId() + "] "
							+ n.getNombre() + " del Espectáculo [id=" + guardado.getId() + "]");
					break;
				}
			}
		}
		for (String l : logsModif)
			logService.registrarOperacion(login, TipoOperacion.ACTUALIZACION, l);
		for (String l : logsBorrado)
			logService.registrarOperacion(login, TipoOperacion.BORRADO, l);

		refrescarDossiersYInforme(guardado);
		return guardado;
	}

	/** Resuelve los artistas de un número borrador a entidades gestionadas. */
	private Set<Artista> resolverArtistas(NumeroBorrador nb, Map<Long, Artista> artistasPorId) {
		return nb.getArtistas().stream().map(a -> artistasPorId.get(a.getId())).filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new));
	}

	/** Compara dos conjuntos de artistas por sus ids. */
	private boolean mismosArtistas(Set<Artista> a, Set<Artista> b) {
		Set<Long> ia = a.stream().map(Artista::getId).collect(Collectors.toSet());
		Set<Long> ib = b.stream().map(Artista::getId).collect(Collectors.toSet());
		return ia.equals(ib);
	}

	/**
	 * Tras guardar, reconstruye las trayectorias de TODOS los artistas que ahora
	 * participan en el espectáculo (estado real) y regenera el informe XML si
	 * existía.
	 */
	private void refrescarDossiersYInforme(Espectaculo guardado) {
		Set<Long> artistasAfectados = guardado.getNumeros().stream().flatMap(n -> n.getArtistas().stream())
				.map(Artista::getId).collect(Collectors.toSet());
		for (Long idArt : artistasAfectados) {
			artistaRepository.findByIdConNumeros(idArt)
					.ifPresent(art -> dossierService.reemplazarTrayectoria(idArt, new ArrayList<>(art.getNumeros())));
		}
		informeXmlService.regenerarSiExiste(guardado);
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
		numeroRepository.flush();
		logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.NUEVO,
				"Nuevo Número [id=" + saved.getId() + "] " + saved.getNombre());
		for (Long idArt : idsArtistas) {
			artistaRepository.findByIdConNumeros(idArt)
					.ifPresent(art -> dossierService.reemplazarTrayectoria(idArt, new ArrayList<>(art.getNumeros())));
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

		// Artistas que estaban antes (para refrescar su dossier aunque salgan del
		// número)
		Set<Long> artistasAntes = n.getArtistas().stream().map(Artista::getId).collect(Collectors.toSet());

		n.setNombre(nombre.trim());
		n.setDuracion(duracion);
		n.setOrden(orden);
		n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
		Numero saved = numeroRepository.save(n);
		numeroRepository.flush();
		logService.registrarOperacion(sesionService.getLoginActual(), TipoOperacion.ACTUALIZACION,
				"Modificación de Número [id=" + saved.getId() + "] " + saved.getNombre());

		// Refrescar el dossier de todos los artistas implicados (los nuevos y los que
		// se hayan quitado), volcando su trayectoria real actual.
		Set<Long> afectados = new HashSet<>(artistasAntes);
		afectados.addAll(idsArtistas);
		for (Long idArt : afectados) {
			artistaRepository.findByIdConNumeros(idArt)
					.ifPresent(art -> dossierService.reemplazarTrayectoria(idArt, new ArrayList<>(art.getNumeros())));
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

		// Artistas que participaban en el número, para refrescar su dossier tras
		// borrarlo
		Set<Long> artistasAfectados = n.getArtistas().stream().map(Artista::getId).collect(Collectors.toSet());

		numeroRepository.delete(n);
		numeroRepository.flush();

		for (Long idArt : artistasAfectados) {
			artistaRepository.findByIdConNumeros(idArt)
					.ifPresent(art -> dossierService.reemplazarTrayectoria(idArt, new ArrayList<>(art.getNumeros())));
		}
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
	/**
	 * Regenera los informes XML de todos los espectáculos dirigidos por un
	 * coordinador. Se llama cuando cambian datos del coordinador (nombre, email,
	 * senior) que aparecen en el XML, para mantenerlo actualizado.
	 */
	@Transactional(readOnly = true)
	public void regenerarInformesDeCoordinador(Long idCoordinador) {
		if (idCoordinador == null)
			return;
		for (Long idEsp : espectaculoRepository.findIdsByCoordinador(idCoordinador)) {
			regenerarInformeXmlSiExiste(idEsp);
		}
	}

	/**
	 * Regenera los informes XML de todos los espectáculos en los que participa un
	 * artista. Se llama cuando cambian datos del artista (nombre, email,
	 * nacionalidad, apodo, especialidades) que aparecen en el XML.
	 */
	@Transactional(readOnly = true)
	public void regenerarInformesDeArtista(Long idArtista) {
		if (idArtista == null)
			return;
		for (Long idEsp : espectaculoRepository.findIdsByArtistaParticipante(idArtista)) {
			regenerarInformeXmlSiExiste(idEsp);
		}
	}

	private void regenerarInformeXmlSiExiste(Long idEspectaculo) {
		if (idEspectaculo == null || !informeXmlService.existeXml(idEspectaculo))
			return;
		// Cargamos el espectáculo y forzamos la inicialización de sus números y
		// artistas SIN reemplazar la colección (un setNumeros() sobre una colección
		// con orphanRemoval lanza "collection no longer referenced"). Basta con
		// recorrer la relación para que Hibernate la cargue dentro de la transacción.
		espectaculoRepository.findById(idEspectaculo).ifPresent(esp -> {
			esp.getNumeros().forEach(n -> n.getArtistas().size());
			informeXmlService.regenerarSiExiste(esp);
		});
	}
}
