package com.damianqm.tarea3adt.modelo.objectdb;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Incidencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime fechaHora;

	@Enumerated(EnumType.STRING)
	private TipoIncidencia tipo;

	@Column(length = 1000)
	private String descripcion;

	private boolean resuelta = false;

	@Column(nullable = false)
	private Long idPersonaReporta;

	private Long idEspectaculo;

	private Long idNumero;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "resolucion_id")
	private ResolucionIncidencia resolucion;

	public Incidencia() {
	}

	public Incidencia(TipoIncidencia tipo, String descripcion, Long idPersonaReporta, Long idEspectaculo,
			Long idNumero) {
		this.fechaHora = LocalDateTime.now();
		this.tipo = tipo;
		this.descripcion = descripcion;
		this.idPersonaReporta = idPersonaReporta;
		this.idEspectaculo = idEspectaculo;
		this.idNumero = idNumero;
		this.resuelta = false;
	}

	// ── Getters / Setters ────────────────────────────────

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public TipoIncidencia getTipo() {
		return tipo;
	}

	public void setTipo(TipoIncidencia tipo) {
		this.tipo = tipo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isResuelta() {
		return resuelta;
	}

	public void setResuelta(boolean resuelta) {
		this.resuelta = resuelta;
	}

	public Long getIdPersonaReporta() {
		return idPersonaReporta;
	}

	public void setIdPersonaReporta(Long idPersonaReporta) {
		this.idPersonaReporta = idPersonaReporta;
	}

	public Long getIdEspectaculo() {
		return idEspectaculo;
	}

	public void setIdEspectaculo(Long idEspectaculo) {
		this.idEspectaculo = idEspectaculo;
	}

	public Long getIdNumero() {
		return idNumero;
	}

	public void setIdNumero(Long idNumero) {
		this.idNumero = idNumero;
	}

	public ResolucionIncidencia getResolucion() {
		return resolucion;
	}

	public void setResolucion(ResolucionIncidencia resolucion) {
		this.resolucion = resolucion;
	}

	@Override
	public String toString() {
		return "[" + id + "] " + tipo + " - " + (resuelta ? "RESUELTA" : "PENDIENTE") + " - " + descripcion;
	}
}
