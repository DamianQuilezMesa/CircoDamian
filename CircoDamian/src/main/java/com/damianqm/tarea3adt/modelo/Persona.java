package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;

/**
 * Clase base de Artista y Coordinacion.
 * Estrategia JOINED: una tabla por subclase unida por id.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    /** Código ISO del país (ES, FR, IT...). */
    @Column(nullable = false)
    private String nacionalidad;

    public Persona() {}

    public Persona(String nombre, String email, String nacionalidad) {
        this.nombre = nombre;
        this.email = email;
        this.nacionalidad = nacionalidad;
    }

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
