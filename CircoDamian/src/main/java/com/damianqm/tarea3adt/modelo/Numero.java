package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/** Número circense: nombre único, duración x,0 o x,5, con ≥1 artista. */
@Entity
@Table(name = "numero")
public class Numero {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false)
	private double duracion;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "numero_artista", joinColumns = @JoinColumn(name = "id_numero"), inverseJoinColumns = @JoinColumn(name = "id_artista"))
	private Set<Artista> artistas = new HashSet<>();

	public Numero() {
	}

	public Numero(String nombre, double duracion) {
		this.nombre = nombre;
		this.duracion = duracion;
	}

	public Long getId() {
		return id;
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

	public Set<Artista> getArtistas() {
		return artistas;
	}

	public void setArtistas(Set<Artista> artistas) {
		this.artistas = artistas;
	}

	/** Formatea duración como "x,0" o "x,5". */
	public String getDuracionFormateada() {
		int entera = (int) duracion;
		boolean medio = (duracion - entera) >= 0.4;
		return entera + "," + (medio ? "5" : "0");
	}
}
