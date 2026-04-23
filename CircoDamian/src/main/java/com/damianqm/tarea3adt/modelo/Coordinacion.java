package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Persona de coordinación. Puede ser senior (con fecha). */
@Entity
@Table(name = "coordinacion")
@PrimaryKeyJoinColumn(name = "id_persona")
public class Coordinacion extends Persona {

    @Column(nullable = false, columnDefinition = "BOOLEAN")
    private boolean senior = false;

    @Column(name = "fecha_senior")
    private LocalDate fechaSenior;

    @OneToMany(mappedBy = "coordinador", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Espectaculo> espectaculos = new ArrayList<>();

    public Coordinacion() {}

    public Coordinacion(String nombre, String email, String nacionalidad,
                        boolean senior, LocalDate fechaSenior) {
        super(nombre, email, nacionalidad);
        this.senior = senior;
        this.fechaSenior = fechaSenior;
    }

    public boolean isSenior() { return senior; }
    public void setSenior(boolean senior) { this.senior = senior; }
    public LocalDate getFechaSenior() { return fechaSenior; }
    public void setFechaSenior(LocalDate fechaSenior) { this.fechaSenior = fechaSenior; }
    public List<Espectaculo> getEspectaculos() { return espectaculos; }
    public void setEspectaculos(List<Espectaculo> espectaculos) { this.espectaculos = espectaculos; }
}
