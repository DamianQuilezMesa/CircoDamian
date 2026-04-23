package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;

/** Tabla intermedia Espectáculo↔Número con el orden en que aparece. */
@Entity
@Table(name = "espectaculo_numero",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_esp_num_orden",
           columnNames = {"id_espectaculo", "orden"}))
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

    @Column(nullable = false)
    private int orden;

    public EspectaculoNumero() {}

    public EspectaculoNumero(Espectaculo espectaculo, Numero numero, int orden) {
        this.espectaculo = espectaculo;
        this.numero = numero;
        this.orden = orden;
    }

    public Espectaculo getEspectaculo() { return espectaculo; }
    public void setEspectaculo(Espectaculo espectaculo) { this.espectaculo = espectaculo; }
    public Numero getNumero() { return numero; }
    public void setNumero(Numero numero) { this.numero = numero; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
