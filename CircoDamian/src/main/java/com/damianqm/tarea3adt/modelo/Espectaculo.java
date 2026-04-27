package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Espectáculo: nombre único (máx 25 chars), periodo ≤1 año, coordinador y ≥3 números. */
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
F
    /** Números con su orden. Cascade + orphanRemoval para limpiar la tabla intermedia. */
    @OneToMany(mappedBy = "espectaculo",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY,
               orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<EspectaculoNumero> numerosEnEspectaculo = new ArrayList<>();

    public Espectaculo() {}

    public Espectaculo(String nombre, LocalDate fechaInicio, LocalDate fechaFin, Coordinacion coordinador) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.coordinador = coordinador;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Coordinacion getCoordinador() { return coordinador; }
    public void setCoordinador(Coordinacion coordinador) { this.coordinador = coordinador; }
    public List<EspectaculoNumero> getNumerosEnEspectaculo() { return numerosEnEspectaculo; }
    public void setNumerosEnEspectaculo(List<EspectaculoNumero> numerosEnEspectaculo) {
        this.numerosEnEspectaculo = numerosEnEspectaculo;
    }
}
