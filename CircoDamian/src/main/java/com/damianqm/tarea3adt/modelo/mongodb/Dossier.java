package com.damianqm.tarea3adt.modelo.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

// Documento MongoDB que agrupa toda la información profesional de un artista:
// datos personales sincronizados desde MySQL, trayectoria de actuaciones,
// evaluaciones emitidas por coordinación y observaciones generales.
@Document(collection = "dossiers")
public class Dossier {

	@Id
	private String id;

	// Clave natural: coincide con el id de Artista (y de Persona) en MySQL
	@Indexed(unique = true)
	private Long idArtista;

	private Long idPersona;
	private String nombre;
	private String email;
	private String nacionalidad;
	private String apodo;
	private List<String> especialidades = new ArrayList<>();
	private List<EntradaTrayectoria> trayectoria = new ArrayList<>();
	private List<Evaluacion> evaluaciones = new ArrayList<>();
	private String observaciones;

	public Dossier() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getIdArtista() {
		return idArtista;
	}

	public void setIdArtista(Long idArtista) {
		this.idArtista = idArtista;
	}

	public Long getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Long idPersona) {
		this.idPersona = idPersona;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNacionalidad() {
		return nacionalidad;
	}

	public void setNacionalidad(String nacionalidad) {
		this.nacionalidad = nacionalidad;
	}

	public String getApodo() {
		return apodo;
	}

	public void setApodo(String apodo) {
		this.apodo = apodo;
	}

	public List<String> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<String> especialidades) {
		this.especialidades = especialidades;
	}

	public List<EntradaTrayectoria> getTrayectoria() {
		return trayectoria;
	}

	public void setTrayectoria(List<EntradaTrayectoria> trayectoria) {
		this.trayectoria = trayectoria;
	}

	public List<Evaluacion> getEvaluaciones() {
		return evaluaciones;
	}

	public void setEvaluaciones(List<Evaluacion> evaluaciones) {
		this.evaluaciones = evaluaciones;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}
}
