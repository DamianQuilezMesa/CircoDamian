package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "numero",
       uniqueConstraints = @UniqueConstraint(name = "uq_num_esp_orden",
               columnNames = {"id_espectaculo", "orden"}))
public class Numero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private double duracion;

    @Column(nullable = false)
    private int orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_espectaculo", nullable = false)
    private Espectaculo espectaculo;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "numero_artista",
               joinColumns        = @JoinColumn(name = "id_numero"),
               inverseJoinColumns = @JoinColumn(name = "id_artista"))
    private Set<Artista> artistas = new HashSet<>();

    public Numero() {}

    public Numero(String nombre, double duracion, int orden, Espectaculo espectaculo) {
        this.nombre      = nombre;
        this.duracion    = duracion;
        this.orden       = orden;
        this.espectaculo = espectaculo;
    }

    public Long getId()                              { return id; }

    public String getNombre()                        { return nombre; }
    public void setNombre(String nombre)             { this.nombre = nombre; }

    public double getDuracion()                      { return duracion; }
    public void setDuracion(double duracion)         { this.duracion = duracion; }

    public int getOrden()                            { return orden; }
    public void setOrden(int orden)                  { this.orden = orden; }

    public Espectaculo getEspectaculo()              { return espectaculo; }
    public void setEspectaculo(Espectaculo e)        { this.espectaculo = e; }

    public Set<Artista> getArtistas()                { return artistas; }
    public void setArtistas(Set<Artista> artistas)   { this.artistas = artistas; }

    public String getDuracionFormateada() {
        int entera = (int) duracion;
        boolean medio = (duracion - entera) >= 0.4;
        return entera + "," + (medio ? "5" : "0");
    }
}
