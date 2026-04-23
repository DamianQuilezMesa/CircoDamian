package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/** Artista del circo. Tiene apodo opcional y una o más especialidades. */
@Entity
@Table(name = "artista")
@PrimaryKeyJoinColumn(name = "id_persona")
public class Artista extends Persona {

    @Column(name = "apodo")
    private String apodo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "artista_especialidad",
        joinColumns = @JoinColumn(name = "id_artista"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_artista_especialidad",
            columnNames = {"id_artista", "especialidad"}))
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidad", nullable = false)
    private Set<Especialidad> especialidades = new HashSet<>();

    @ManyToMany(mappedBy = "artistas", fetch = FetchType.LAZY)
    private Set<Numero> numeros = new HashSet<>();

    public Artista() {}

    public Artista(String nombre, String email, String nacionalidad,
                   String apodo, Set<Especialidad> especialidades) {
        super(nombre, email, nacionalidad);
        this.apodo = apodo;
        this.especialidades = especialidades;
    }

    public String getApodo() { return apodo; }
    public void setApodo(String apodo) { this.apodo = apodo; }
    public Set<Especialidad> getEspecialidades() { return especialidades; }
    public void setEspecialidades(Set<Especialidad> especialidades) { this.especialidades = especialidades; }
    public Set<Numero> getNumeros() { return numeros; }
    public void setNumeros(Set<Numero> numeros) { this.numeros = numeros; }
}
