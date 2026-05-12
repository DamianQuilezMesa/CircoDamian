package com.damianqm.tarea3adt.repositorios.db4o;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.damianqm.tarea3adt.modelo.db4o.LogOperacion;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositorio para operaciones CRUD sobre DB4O (base de datos embebida).
 * Gestiona el historial de operaciones del sistema (CU7 y CU10).
 */
@Repository
public class LogDb4oRepository {

    private static final String DB_PATH = "ficheros/log.db4o";

    private ObjectContainer db;
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void init() {
        db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DB_PATH);
        // Inicializar el contador de id con el máximo existente
        List<LogOperacion> todos = obtenerTodos();
        long maxId = todos.stream()
                .filter(l -> l.getId() != null)
                .mapToLong(LogOperacion::getId)
                .max()
                .orElse(0L);
        idCounter.set(maxId + 1);
    }

    @PreDestroy
    public void cerrar() {
        if (db != null && !db.ext().isClosed()) {
            db.close();
        }
    }

    /**
     * CU7 – Guarda un nuevo LogOperacion en DB4O de forma transaccional.
     */
    public void guardar(LogOperacion log) {
        log.setId(idCounter.getAndIncrement());
        try {
            db.store(log);
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw new RuntimeException("Error al guardar el log en DB4O", e);
        }
    }

    /**
     * CU10 – Consulta el historial con filtros combinados usando Native Query.
     *
     * @param usuario        Nombre de usuario exacto (obligatorio, no nulo ni vacío).
     * @param tipos          Conjunto de TipoOperacion a incluir; null o vacío = todos.
     * @param desde          Fecha/hora de inicio del rango (inclusive); null = sin límite inferior.
     * @param hasta          Fecha/hora de fin del rango (inclusive); null = sin límite superior.
     * @return Lista de LogOperacion que cumplen los criterios.
     */
    public List<LogOperacion> consultar(String usuario,
                                        Set<TipoOperacion> tipos,
                                        LocalDateTime desde,
                                        LocalDateTime hasta) {

        final String usuarioFiltro = (usuario != null) ? usuario.trim() : "";
        final boolean filtrarTipos = tipos != null && !tipos.isEmpty();

        // Native Query de DB4O
        ObjectSet<LogOperacion> result = db.query(new Predicate<LogOperacion>() {
            @Override
            public boolean match(LogOperacion log) {
                // Filtro por usuario (obligatorio)
                if (!usuarioFiltro.isEmpty() &&
                    !usuarioFiltro.equalsIgnoreCase(log.getUsuario())) {
                    return false;
                }
                // Filtro por tipo de operación
                if (filtrarTipos && !tipos.contains(log.getTipoOperacion())) {
                    return false;
                }
                // Filtro por fecha inicio
                if (desde != null && log.getFechaHora() != null &&
                    log.getFechaHora().isBefore(desde)) {
                    return false;
                }
                // Filtro por fecha fin
                if (hasta != null && log.getFechaHora() != null &&
                    log.getFechaHora().isAfter(hasta)) {
                    return false;
                }
                return true;
            }
        });

        return new ArrayList<>(result);
    }

    /**
     * Devuelve todos los registros (útil para inicializar el contador de IDs).
     */
    private List<LogOperacion> obtenerTodos() {
        ObjectSet<LogOperacion> result = db.query(LogOperacion.class);
        return new ArrayList<>(result);
    }

    /**
     * Devuelve lista de nombres de usuario distintos que tienen registros de log.
     * Útil para autocompletar el campo usuario en la pantalla CU10.
     */
    public List<String> obtenerUsuariosDistintos() {
        return obtenerTodos().stream()
                .map(LogOperacion::getUsuario)
                .filter(u -> u != null && !u.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}
