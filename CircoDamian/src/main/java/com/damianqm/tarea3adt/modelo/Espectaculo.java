package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Espectáculo del circo.
 * - Nombre único, máximo 25 caracteres.
 * - Periodo de fechas de vigencia (inicio y fin), no superior a 1 año.
 * - Dirigido por una sola persona de Coordinación.
 * - Compuesto de al menos 3 números circenses (via EspectaculoNumero).
 */
@Entity
@Table(name = "espectaculo")
public class Espectaculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 25)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_coordinador", nullable = false)
    private Coordinacion coordinador;

    /**
     * Relación con los números a través de la tabla espectaculo_numero.
     * cascade=ALL + orphanRemoval=true: al eliminar la relación se borra la fila
     * de la tabla intermedia (NO el número en sí, que es independiente).
     */
    @OneToMany(mappedBy = "espectaculo",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<EspectaculoNumero> numerosEnEspectaculo = new ArrayList<>();

    // ─── Constructores ────────────────────────────────────────────────
    public Espectaculo() {}

    public Espectaculo(String nombre, LocalDate fechaInicio, LocalDate fechaFin,
                       Coordinacion coordinador) {
        this.nombre      = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = fechaFin;
        this.coordinador = coordinador;
    }

    // ─── Getters / Setters ────────────────────────────────────────────
    public Long getId()         { return id; }
    public void setId(Long id)  { this.id = id; }

    public String getNombre()          { return nombre; }
    public void   setNombre(String n)  { this.nombre = n; }

    public LocalDate getFechaInicio()             { return fechaInicio; }
    public void      setFechaInicio(LocalDate d)  { this.fechaInicio = d; }

    public LocalDate getFechaFin()              { return fechaFin; }
    public void      setFechaFin(LocalDate d)   { this.fechaFin = d; }

    public Coordinacion getCoordinador()              { return coordinador; }
    public void         setCoordinador(Coordinacion c){ this.coordinador = c; }

    public List<EspectaculoNumero> getNumerosEnEspectaculo()                         { return numerosEnEspectaculo; }
    public void                    setNumerosEnEspectaculo(List<EspectaculoNumero> l){ this.numerosEnEspectaculo = l; }

    @Override
    public String toString() {
        return "[" + id + "] " + nombre + " (" + fechaInicio + " → " + fechaFin + ")";
    }
}
