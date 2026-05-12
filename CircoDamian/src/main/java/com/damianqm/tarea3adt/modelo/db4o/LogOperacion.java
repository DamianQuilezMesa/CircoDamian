package com.damianqm.tarea3adt.modelo.db4o;

import java.time.LocalDateTime;

/**
 * POJO persistido en DB4O (no es entidad JPA).
 * Representa una operación de auditoría realizada en el sistema.
 */
public class LogOperacion {

    private Long id;
    private LocalDateTime fechaHora;
    private String usuario;
    private TipoOperacion tipoOperacion;
    private String resumen;

    public LogOperacion() {
    }

    public LogOperacion(String usuario, TipoOperacion tipoOperacion, String resumen) {
        this.fechaHora = LocalDateTime.now();
        this.usuario = usuario;
        this.tipoOperacion = tipoOperacion;
        this.resumen = resumen;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    @Override
    public String toString() {
        return "[" + fechaHora + "] " + usuario + " | " + tipoOperacion + " | " + resumen;
    }
}
