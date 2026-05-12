package com.damianqm.tarea3adt.modelo.objectdb;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad persistida en ObjectDB que documenta la resolución de una incidencia.
 * Relación unidireccional hacia {@link Incidencia}.
 */
@Entity
public class ResolucionIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora en que se registra la resolución (automático). */
    private LocalDateTime fechahoraResolucion;

    /** Descripción de las acciones realizadas para resolver la incidencia. */
    @Column(length = 2000)
    private String accionesRealizadas;

    /** Id de la persona que resuelve la incidencia. */
    private Long idPersonaResuelve;

    /** Relación unidireccional con la incidencia resuelta. */
    @ManyToOne
    @JoinColumn(name = "incidencia_id")
    private Incidencia incidencia;

    public ResolucionIncidencia() {
    }

    public ResolucionIncidencia(String accionesRealizadas, Long idPersonaResuelve, Incidencia incidencia) {
        this.fechahoraResolucion = LocalDateTime.now();
        this.accionesRealizadas = accionesRealizadas;
        this.idPersonaResuelve = idPersonaResuelve;
        this.incidencia = incidencia;
    }

    // ── Getters / Setters ────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechahoraResolucion() { return fechahoraResolucion; }
    public void setFechahoraResolucion(LocalDateTime fechahoraResolucion) {
        this.fechahoraResolucion = fechahoraResolucion;
    }

    public String getAccionesRealizadas() { return accionesRealizadas; }
    public void setAccionesRealizadas(String accionesRealizadas) {
        this.accionesRealizadas = accionesRealizadas;
    }

    public Long getIdPersonaResuelve() { return idPersonaResuelve; }
    public void setIdPersonaResuelve(Long idPersonaResuelve) { this.idPersonaResuelve = idPersonaResuelve; }

    public Incidencia getIncidencia() { return incidencia; }
    public void setIncidencia(Incidencia incidencia) { this.incidencia = incidencia; }

    @Override
    public String toString() {
        return "ResolucionIncidencia{id=" + id + ", incidencia=" +
                (incidencia != null ? incidencia.getId() : "null") + "}";
    }
}
