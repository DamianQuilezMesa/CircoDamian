package com.damianqm.tarea3adt.modelo.db4o;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * POJO persistido en DB4O.
 * Todos los campos son String o Long para evitar problemas de compatibilidad
 * entre DB4O y el sistema de módulos de Java 21, que impide el acceso
 * reflectivo a tipos como Enum o LocalDateTime.
 */
public class LogOperacion {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private Long   id;
    private String fechaHoraStr;
    private String usuario;
    private String tipoOperacion;  // guardado como String, no como enum
    private String resumen;

    public LogOperacion() {}

    public LogOperacion(String usuario, TipoOperacion tipoOperacion, String resumen) {
        this.fechaHoraStr  = LocalDateTime.now().format(FMT);
        this.usuario       = usuario;
        this.tipoOperacion = tipoOperacion != null ? tipoOperacion.name() : "";
        this.resumen       = resumen;
    }

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getFechaHoraStr()      { return fechaHoraStr; }
    public void setFechaHoraStr(String s){ this.fechaHoraStr = s; }

    public LocalDateTime getFechaHora() {
        try {
            return fechaHoraStr != null ? LocalDateTime.parse(fechaHoraStr, FMT) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getUsuario()           { return usuario; }
    public void setUsuario(String u)     { this.usuario = u; }

    /** Devuelve el tipo como String (ej: "NUEVO", "ACTUALIZACION", "BORRADO"). */
    public String getTipoOperacion()     { return tipoOperacion; }
    public void setTipoOperacion(String t){ this.tipoOperacion = t; }

    /** Devuelve el tipo como enum (para uso en la capa de presentación). */
    public TipoOperacion getTipoOperacionEnum() {
        try {
            return tipoOperacion != null ? TipoOperacion.valueOf(tipoOperacion) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getResumen()           { return resumen; }
    public void setResumen(String r)     { this.resumen = r; }

    @Override
    public String toString() {
        return "[" + fechaHoraStr + "] " + usuario + " | " + tipoOperacion + " | " + resumen;
    }
}
