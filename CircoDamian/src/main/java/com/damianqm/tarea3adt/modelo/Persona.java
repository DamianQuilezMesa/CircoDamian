package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;

/**
 * Entidad base que representa a cualquier persona que trabaja en el circo.
 * Datos: id, nombre, email (único), nacionalidad.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nacionalidad;

    // ─── Constructores ────────────────────────────────────────────────
    public Persona() {}

    public Persona(String nombre, String email, String nacionalidad) {
        this.nombre = nombre;
        this.email = email;
        this.nacionalidad = nacionalidad;
    }

    // ─── Getters / Setters ────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}
