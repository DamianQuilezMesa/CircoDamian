package com.damianqm.tarea3adt.modelo.mongodb;

// Documento embebido que recoge una observación o anotación interna sobre un
// artista (fecha, texto libre y autor que la registra). Se van adjuntando al
// dossier mediante $push, sin sobrescribir las anteriores.
public class Observacion {

	private String fecha;
	private String texto;
	private String autor;

	public Observacion() {
	}

	public Observacion(String fecha, String texto, String autor) {
		this.fecha = fecha;
		this.texto = texto;
		this.autor = autor;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}
}
