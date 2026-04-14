package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Persona con perfil de Artista.
 * Tiene un apodo opcional y una o más especialidades.
 * Puede participar en varios números de espectáculos.
 */
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
                    columnNames = {"id_artista", "especialidad"}
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidad", nullable = false)
    private Set<Especialidad> especialidades = new HashSet<>();

    @ManyToMany(mappedBy = "artistas", fetch = FetchType.LAZY)
    private Set<Numero> numeros = new HashSet<>();

    // ─── Constructores ────────────────────────────────────────────────
    public Artista() {}

    public Artista(String nombre, String email, String nacionalidad,
                   String apodo, Set<Especialidad> especialidades) {
        super(nombre, email, nacionalidad);
        this.apodo = apodo;
        this.especialidades = especialidades;
    }

    // ─── Getters / Setters ────────────────────────────────────────────
    public String getApodo() { return apodo; }
    public void setApodo(String apodo) { this.apodo = apodo; }

    public Set<Especialidad> getEspecialidades() { return especialidades; }
    public void setEspecialidades(Set<Especialidad> especialidades) { this.especialidades = especialidades; }

    public Set<Numero> getNumeros() { return numeros; }
    public void setNumeros(Set<Numero> numeros) { this.numeros = numeros; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getNombre());
        if (apodo != null && !apodo.isBlank()) sb.append(" \"").append(apodo).append("\"");
        if (!especialidades.isEmpty()) sb.append(" ").append(especialidades);
        return sb.toString();
    }
}
