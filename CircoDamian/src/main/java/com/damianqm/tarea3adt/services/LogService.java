package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.db4o.LogOperacion;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import com.damianqm.tarea3adt.repositorios.db4o.LogDb4oRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Servicio Spring para la gestión del historial de operaciones con DB4O.
 * <ul>
 *   <li>CU7  – registrarOperacion: persiste un log cada vez que ocurre una alta/modificación/borrado.</li>
 *   <li>CU10 – consultarHistorial: permite al administrador filtrar el historial.</li>
 * </ul>
 */
@Service
public class LogService {

    @Autowired
    private LogDb4oRepository logRepo;

    // ─────────────────────────────────────────────────────
    // CU7 – Registrar operación
    // ─────────────────────────────────────────────────────

    /**
     * Registra una operación en el historial DB4O.
     *
     * @param usuario        Nombre de usuario que realizó la acción.
     * @param tipoOperacion  NUEVO | ACTUALIZACION | BORRADO
     * @param resumen        Descripción breve (entidad afectada + id).
     */
    public void registrarOperacion(String usuario, TipoOperacion tipoOperacion, String resumen) {
        LogOperacion log = new LogOperacion(usuario, tipoOperacion, resumen);
        logRepo.guardar(log);
    }

    // ─────────────────────────────────────────────────────
    // CU10 – Consultar historial de operaciones
    // ─────────────────────────────────────────────────────

    /**
     * Consulta el historial de un usuario concreto con filtros opcionales.
     *
     * @param usuario  Nombre de usuario (obligatorio).
     * @param tipos    Tipos de operación a incluir; null o vacío = todos.
     * @param desde    Inicio del rango de fechas (inclusive); null = sin límite.
     * @param hasta    Fin del rango de fechas (inclusive); null = sin límite.
     * @return Lista de {@link LogOperacion} que cumplen los criterios, ordenada por fechaHora desc.
     */
    public List<LogOperacion> consultarHistorial(String usuario,
                                                  Set<TipoOperacion> tipos,
                                                  LocalDateTime desde,
                                                  LocalDateTime hasta) {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("Debe indicar un nombre de usuario para consultar el historial.");
        }
        List<LogOperacion> resultado = logRepo.consultar(usuario, tipos, desde, hasta);
        // Ordenar por fecha descendente
        resultado.sort((a, b) -> {
            if (a.getFechaHora() == null && b.getFechaHora() == null) return 0;
            if (a.getFechaHora() == null) return 1;
            if (b.getFechaHora() == null) return -1;
            return b.getFechaHora().compareTo(a.getFechaHora());
        });
        return resultado;
    }

    /**
     * Devuelve los usuarios distintos que tienen registros en el historial.
     */
    public List<String> obtenerUsuariosConLog() {
        return logRepo.obtenerUsuariosDistintos();
    }
}
