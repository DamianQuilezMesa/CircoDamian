package com.damianqm.tarea3adt.modelo.mongodb;

// Documento embebido que representa una evaluación emitida por coordinación
// sobre un artista (comentario libre + nivel cualitativo + fecha de emisión).
public class Evaluacion {

	private String comentario;
	private String nivel;
	private String fecha;
	private RealizadaPor realizadaPor;

	public Evaluacion() {
	}

	public Evaluacion(String comentario, String nivel, String fecha) {
		this.comentario = comentario;
		this.nivel = nivel;
		this.fecha = fecha;
	}

	public Evaluacion(String comentario, String nivel, String fecha, RealizadaPor realizadaPor) {
		this.comentario = comentario;
		this.nivel = nivel;
		this.fecha = fecha;
		this.realizadaPor = realizadaPor;
	}

	public RealizadaPor getRealizadaPor() {
		return realizadaPor;
	}

	public void setRealizadaPor(RealizadaPor realizadaPor) {
		this.realizadaPor = realizadaPor;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getNivel() {
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
}
