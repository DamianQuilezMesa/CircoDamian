package com.damianqm.tarea3adt.modelo.mongodb;

// Documento embebido que recoge la participación de un artista en un número
// concreto de un espectáculo (identifica el número y el espectáculo al que pertenece).
public class EntradaTrayectoria {

	private Long idEspectaculo;
	private String nombreEspectaculo;
	private Long idNumero;
	private String nombreNumero;
	private int orden;
	private double duracion;

	public EntradaTrayectoria() {
	}

	public EntradaTrayectoria(Long idEspectaculo, String nombreEspectaculo, Long idNumero, String nombreNumero,
			int orden, double duracion) {
		this.idEspectaculo = idEspectaculo;
		this.nombreEspectaculo = nombreEspectaculo;
		this.idNumero = idNumero;
		this.nombreNumero = nombreNumero;
		this.orden = orden;
		this.duracion = duracion;
	}

	public Long getIdEspectaculo() {
		return idEspectaculo;
	}

	public void setIdEspectaculo(Long idEspectaculo) {
		this.idEspectaculo = idEspectaculo;
	}

	public String getNombreEspectaculo() {
		return nombreEspectaculo;
	}

	public void setNombreEspectaculo(String nombreEspectaculo) {
		this.nombreEspectaculo = nombreEspectaculo;
	}

	public Long getIdNumero() {
		return idNumero;
	}

	public void setIdNumero(Long idNumero) {
		this.idNumero = idNumero;
	}

	public String getNombreNumero() {
		return nombreNumero;
	}

	public void setNombreNumero(String nombreNumero) {
		this.nombreNumero = nombreNumero;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public double getDuracion() {
		return duracion;
	}

	public void setDuracion(double duracion) {
		this.duracion = duracion;
	}
}
