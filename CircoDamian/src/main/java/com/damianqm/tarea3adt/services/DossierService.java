package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.Artista;
import com.damianqm.tarea3adt.modelo.Numero;
import com.damianqm.tarea3adt.modelo.mongodb.Dossier;
import com.damianqm.tarea3adt.modelo.mongodb.EntradaTrayectoria;
import com.damianqm.tarea3adt.modelo.mongodb.Evaluacion;
import com.damianqm.tarea3adt.modelo.mongodb.Observacion;
import com.damianqm.tarea3adt.modelo.mongodb.RealizadaPor;
import com.damianqm.tarea3adt.repositorios.mongodb.DossierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DossierService {

	@Autowired
	private DossierRepository dossierRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SesionService sesionService;

	// CU3A: crea el dossier al registrar un artista nuevo
	public void crearDossier(Artista artista) {
		Dossier d = new Dossier();
		d.setIdPersona(artista.getId());
		d.setIdArtista(artista.getId());
		d.setNombre(artista.getNombre());
		d.setEmail(artista.getEmail());
		d.setNacionalidad(artista.getNacionalidad());
		d.setApodo(artista.getApodo());
		d.setEspecialidades(artista.getEspecialidades().stream().map(Enum::name).collect(Collectors.toList()));
		d.setTrayectoria(new ArrayList<>());
		d.setEvaluaciones(new ArrayList<>());
		d.setObservaciones(new ArrayList<>());
		dossierRepository.save(d);
	}

	// CU3C parte 1: actualiza nombre, email y nacionalidad cuando se modifican los
	// datos de Persona
	// Si la persona no es artista no existe dossier y la operación no hace nada.
	public void actualizarDatosPersonales(Long idPersona, String nombre, String email, String nacionalidad) {
		Query q = new Query(Criteria.where("idArtista").is(idPersona));
		Update u = new Update().set("nombre", nombre).set("email", email).set("nacionalidad", nacionalidad);
		mongoTemplate.updateFirst(q, u, Dossier.class);
	}

	// CU3C parte 2: actualiza apodo y especialidades cuando se modifican los datos
	// específicos de Artista
	public void actualizarDatosArtista(Long idArtista, String apodo, List<String> especialidades) {
		Query q = new Query(Criteria.where("idArtista").is(idArtista));
		Update u = new Update().set("apodo", apodo).set("especialidades", especialidades);
		mongoTemplate.updateFirst(q, u, Dossier.class);
	}

	// CU5: añade o actualiza la entrada de trayectoria del artista para el número
	// indicado.
	// Si ya existe una entrada con el mismo idNumero se sustituye (puede haber
	// cambiado
	// el nombre, orden o duración del número); en caso contrario se añade al final.
	public void agregarOActualizarTrayectoria(Long idArtista, Numero numero) {
		Optional<Dossier> opt = dossierRepository.findByIdArtista(idArtista);
		if (opt.isEmpty())
			return;

		Dossier d = opt.get();
		EntradaTrayectoria entrada = new EntradaTrayectoria(numero.getEspectaculo().getId(),
				numero.getEspectaculo().getNombre(), numero.getId(), numero.getNombre(), numero.getOrden(),
				numero.getDuracion());

		List<EntradaTrayectoria> tray = d.getTrayectoria();
		boolean actualizado = false;
		for (int i = 0; i < tray.size(); i++) {
			if (tray.get(i).getIdNumero().equals(numero.getId())) {
				tray.set(i, entrada);
				actualizado = true;
				break;
			}
		}
		if (!actualizado) {
			tray.add(entrada);
		}
		dossierRepository.save(d);
	}

	// CU12: añade una evaluación al dossier del artista usando $push (actualización
	// parcial).
	// La evaluación queda sellada con quién la realiza (idPersona + rol), tal como
	// muestra el ejemplo del enunciado.
	public void agregarEvaluacion(Long idArtista, String comentario, String nivel) {
		if (comentario == null || comentario.isBlank())
			throw new IllegalArgumentException("El comentario de la evaluación es obligatorio.");
		if (nivel == null || nivel.isBlank())
			throw new IllegalArgumentException("Selecciona un nivel para la evaluación.");

		RealizadaPor realizadaPor = new RealizadaPor(sesionService.getIdPersonaActual(),
				sesionService.getPerfilActual() != null ? sesionService.getPerfilActual().name() : null);
		Evaluacion ev = new Evaluacion(comentario.trim(), nivel.trim(), LocalDate.now().toString(), realizadaPor);
		Query q = new Query(Criteria.where("idArtista").is(idArtista));
		Update u = new Update().push("evaluaciones", ev);
		mongoTemplate.updateFirst(q, u, Dossier.class);
	}

	// CU12: añade una observación al dossier del artista usando $push
	// (actualización
	// parcial). Cada observación lleva fecha, texto y autor, y se ADJUNTA sin
	// sobrescribir las anteriores.
	public void agregarObservacion(Long idArtista, String texto) {
		if (texto == null || texto.isBlank())
			throw new IllegalArgumentException("El texto de la observación es obligatorio.");

		Observacion obs = new Observacion(LocalDate.now().toString(), texto.trim(), sesionService.getLoginActual());
		Query q = new Query(Criteria.where("idArtista").is(idArtista));
		Update u = new Update().push("observaciones", obs);
		mongoTemplate.updateFirst(q, u, Dossier.class);
	}

	public Optional<Dossier> buscarPorArtista(Long idArtista) {
		return dossierRepository.findByIdArtista(idArtista);
	}

	public List<Dossier> findAll() {
		return dossierRepository.findAll();
	}
}
