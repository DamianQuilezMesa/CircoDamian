package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Número circense independiente.
 * Un número puede participar en varios espectáculos (con diferente orden en cada uno).
 * La relación con Espectaculo se gestiona a través de EspectaculoNumero.
 * La relación con Artista se gestiona a través de la tabla numero_artista.
 */
@Entity
@Table(name = "numero")
public class Numero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Duración en minutos: parte decimal solo puede ser .0 o .5 */
    @Column(nullable = false)
    private double duracion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "numero_artista",
        joinColumns = @JoinColumn(name = "id_numero"),
        inverseJoinColumns = @JoinColumn(name = "id_artista")
    )
    private Set<Artista> artistas = new HashSet<>();

    // ─── Constructores ────────────────────────────────────────────────
    public Numero() {}

    public Numero(String nombre, double duracion) {
        this.nombre   = nombre;
        this.duracion = duracion;
    }

    // ─── Getters / Setters ────────────────────────────────────────────
    public Long   getId()       { return id; }
    public void   setId(Long id){ this.id = id; }

    public String getNombre()         { return nombre; }
    public void   setNombre(String n) { this.nombre = n; }

    public double getDuracion()          { return duracion; }
    public void   setDuracion(double d)  { this.duracion = d; }

    public Set<Artista> getArtistas()               { return artistas; }
    public void         setArtistas(Set<Artista> a) { this.artistas = a; }

    /** Formatea duración como "x,0" o "x,5" según el enunciado */
    public String getDuracionFormateada() {
        int parte  = (int) duracion;
        boolean medio = (duracion - parte) >= 0.4;
        return parte + "," + (medio ? "5" : "0");
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nombre + " (" + getDuracionFormateada() + " min)";
    }
}
