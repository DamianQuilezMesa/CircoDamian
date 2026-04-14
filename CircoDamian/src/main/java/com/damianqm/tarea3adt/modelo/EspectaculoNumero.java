package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;

/**
 * Tabla de relación entre Espectaculo y Numero.
 * Almacena el orden que tiene cada número dentro de cada espectáculo.
 * Un número puede aparecer en múltiples espectáculos con diferente orden.
 *
 * Tabla: espectaculo_numero (id_espectaculo, id_numero, orden)
 * PK compuesta: (id_espectaculo, id_numero)
 * Restricción única: (id_espectaculo, orden) — sin dos números en la misma posición.
 */
@Entity
@Table(name = "espectaculo_numero",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_esp_num_orden",
                             columnNames = {"id_espectaculo", "orden"})
       })
@IdClass(EspectaculoNumeroId.class)
public class EspectaculoNumero {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espectaculo", nullable = false)
    private Espectaculo espectaculo;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_numero", nullable = false)
    private Numero numero;

    /** Posición del número dentro de este espectáculo (1, 2, 3…) */
    @Column(nullable = false)
    private int orden;

    // ─── Constructores ────────────────────────────────────────────────
    public EspectaculoNumero() {}

    public EspectaculoNumero(Espectaculo espectaculo, Numero numero, int orden) {
        this.espectaculo = espectaculo;
        this.numero      = numero;
        this.orden       = orden;
    }

    // ─── Getters / Setters ────────────────────────────────────────────
    public Espectaculo getEspectaculo()              { return espectaculo; }
    public void        setEspectaculo(Espectaculo e) { this.espectaculo = e; }

    public Numero getNumero()         { return numero; }
    public void   setNumero(Numero n) { this.numero = n; }

    public int  getOrden()      { return orden; }
    public void setOrden(int o) { this.orden = o; }

    /** Para mostrar en tablas: orden + nombre + duración */
    public String getResumen() {
        return orden + ". " + numero.getNombre()
               + " (" + numero.getDuracionFormateada() + " min)";
    }
}
