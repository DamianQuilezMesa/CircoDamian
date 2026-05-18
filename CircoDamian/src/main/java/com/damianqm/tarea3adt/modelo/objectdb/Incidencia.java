package com.damianqm.tarea3adt.modelo.objectdb;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad persistida en ObjectDB (servidor). Representa una incidencia técnica,
 * artística u organizativa del circo.
 */
@Entity
public class Incidencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Fecha y hora en que se registra la incidencia (automático). */
	private LocalDateTime fechaHora;

	/** Tipo de incidencia. */
	@Enumerated(EnumType.STRING)
	private TipoIncidencia tipo;

	/** Descripción abierta, hasta 1000 caracteres. */
	@Column(length = 1000)
	private String descripcion;

	/** Indica si la incidencia está resuelta. Por defecto false. */
	private boolean resuelta = false;

	/** Id de la persona que reporta la incidencia (obligatorio). */
	@Column(nullable = false)
	private Long idPersonaReporta;

	/** Id del espectáculo afectado (opcional). */
	private Long idEspectaculo;

	/** Id del número circense afectado (opcional). */
	private Long idNumero;

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

	@Override
	public String toString() {
		return "[" + id + "] " + tipo + " - " + (resuelta ? "RESUELTA" : "PENDIENTE") + " - " + descripcion;
	}
}
