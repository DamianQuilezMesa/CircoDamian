package com.damianqm.tarea3adt.dto;

import com.damianqm.tarea3adt.modelo.Coordinacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Espectáculo en construcción que vive solo en memoria (en sesión) mientras el
 * usuario lo monta o lo edita. Agrupa los datos básicos y la lista de números
 * borrador. Nada de esto se persiste hasta pulsar "Guardar espectáculo",
 * momento en el que el servicio valida el conjunto completo y lo guarda en una
 * sola transacción.
 */
public class EspectaculoBorrador {

	/** id del espectáculo ya persistido (null si es nuevo). */
	private Long id;
	private String nombre;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private Coordinacion coordinador;
	private final List<NumeroBorrador> numeros = new ArrayList<>();

	public EspectaculoBorrador() {
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

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Coordinacion getCoordinador() {
		return coordinador;
	}

	public void setCoordinador(Coordinacion coordinador) {
		this.coordinador = coordinador;
	}

	public List<NumeroBorrador> getNumeros() {
		return numeros;
	}

	public boolean esNuevo() {
		return id == null;
	}

	/** Siguiente orden libre (el mayor actual + 1). */
	public int siguienteOrden() {
		int max = 0;
		for (NumeroBorrador n : numeros) {
			if (n.getOrden() > max)
				max = n.getOrden();
		}
		return max + 1;
	}
}
