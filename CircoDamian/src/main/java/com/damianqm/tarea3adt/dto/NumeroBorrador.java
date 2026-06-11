package com.damianqm.tarea3adt.dto;

import com.damianqm.tarea3adt.modelo.Artista;

import java.util.HashSet;
import java.util.Set;

/**
 * Número que vive solo en memoria mientras se monta o edita un espectáculo,
 * antes de persistirlo. No es una entidad JPA: no toca la base de datos hasta
 * que se confirma el guardado completo del espectáculo.
 */
public class NumeroBorrador {

	/** id del número ya persistido (null si es nuevo y aún no existe en BD). */
	private Long id;
	private String nombre;
	private double duracion;
	private int orden;
	private Set<Artista> artistas = new HashSet<>();

	public NumeroBorrador() {
	}

	public NumeroBorrador(Long id, String nombre, double duracion, int orden, Set<Artista> artistas) {
		this.id = id;
		this.nombre = nombre;
		this.duracion = duracion;
		this.orden = orden;
		this.artistas = artistas != null ? new HashSet<>(artistas) : new HashSet<>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getDuracion() {
		return duracion;
	}

	public void setDuracion(double duracion) {
		this.duracion = duracion;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public Set<Artista> getArtistas() {
		return artistas;
	}

	public void setArtistas(Set<Artista> artistas) {
		this.artistas = artistas != null ? new HashSet<>(artistas) : new HashSet<>();
	}

	/** Duración mostrada como x,0 / x,5. */
	public String getDuracionFormateada() {
		int entera = (int) duracion;
		boolean medio = (duracion - entera) >= 0.4;
		return entera + "," + (medio ? "5" : "0");
	}
}
