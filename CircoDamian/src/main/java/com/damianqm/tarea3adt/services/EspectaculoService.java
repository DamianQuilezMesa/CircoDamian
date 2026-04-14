package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EspectaculoService {

    @Autowired private EspectaculoRepository       espectaculoRepository;
    @Autowired private EspectaculoNumeroRepository enRepository;
    @Autowired private NumeroRepository            numeroRepository;
    @Autowired private CoordinacionRepository      coordinacionRepository;
    @Autowired private ArtistaRepository           artistaRepository;

    // ─── Consultas básicas ────────────────────────────────────────────
    public List<Espectaculo> findAll()        { return espectaculoRepository.findAllOrdenados(); }
    public List<Numero>      findAllNumeros() { return numeroRepository.findAllConArtistas(); }

    /**
     * Carga un espectáculo COMPLETO sin duplicados usando 3 queries independientes:
     *  1. Carga el Espectaculo (coordinador EAGER).
     *  2. Carga los EspectaculoNumero con Numero (EAGER) ordenados por orden.
     *  3. Para cada Numero, carga sus artistas con una query DISTINCT.
     * Al no hacer JOIN FETCH sobre colecciones en una sola query, es
     * imposible que Hibernate duplique filas en ninguna colección.
     */
    @Transactional(readOnly = true)
    public Optional<Espectaculo> findByIdCompleto(Long id) {
        Optional<Espectaculo> opt = espectaculoRepository.findById(id);
        opt.ifPresent(esp -> {
            // Query 2: EspectaculoNumero ordenados — Numero es EAGER, se carga automáticamente
            List<EspectaculoNumero> relaciones =
                    enRepository.findByEspectaculoIdOrderByOrdenAsc(esp.getId());
            // Query 3: artistas de cada número, sin duplicados
            for (EspectaculoNumero en : relaciones) {
                Numero n = en.getNumero();
                numeroRepository.findByIdConArtistas(n.getId())
                        .ifPresent(nConArts -> n.setArtistas(nConArts.getArtistas()));
            }
            // Poblar la colección del espectáculo con los datos ya cargados
            esp.setNumerosEnEspectaculo(relaciones);
        });
        return opt;
    }

    public Optional<Espectaculo> findById(Long id)      { return espectaculoRepository.findById(id); }
    public Optional<Espectaculo> findByNombre(String n) { return espectaculoRepository.findByNombre(n.trim()); }

    public List<EspectaculoNumero> findNumerosDeEspectaculo(Long idEsp) {
        return enRepository.findByEspectaculoIdOrderByOrdenAsc(idEsp);
    }

    // ─── CU5A: Espectáculo ────────────────────────────────────────────

    /** Valida datos del espectáculo sin persistir nada */
    public void validarDatosEspectaculo(String nombre, LocalDate inicio, LocalDate fin,
                                        Long idCoord, Long idExcluir) {
        validarEspectaculo(nombre, inicio, fin, idExcluir);
        if (idCoord == null)
            throw new IllegalArgumentException("Debe seleccionar un coordinador.");
        coordinacionRepository.findById(idCoord)
                .orElseThrow(() -> new IllegalArgumentException("Coordinador no encontrado."));
    }

    /**
     * Crea un espectáculo con sus números en una sola transacción.
     * Solo acepta números ya existentes en la BD (idNumeroExistente != null).
     * Requiere ≥3 números.
     */
    @Transactional
    public Espectaculo persistirEspectaculoCompleto(
            String nombre, LocalDate inicio, LocalDate fin,
            Long idCoord, List<NumeroEnEspectaculo> numerosAsignados,
            Long idEspExistente) {

        validarEspectaculo(nombre, inicio, fin, idEspExistente);
        if (idCoord == null)
            throw new IllegalArgumentException("Debe seleccionar un coordinador.");
        Coordinacion coord = coordinacionRepository.findById(idCoord)
                .orElseThrow(() -> new IllegalArgumentException("Coordinador no encontrado."));

        if (numerosAsignados == null || numerosAsignados.size() < 3)
            throw new IllegalArgumentException(
                    "El espectáculo necesita al menos 3 números (tiene " +
                    (numerosAsignados == null ? 0 : numerosAsignados.size()) + ").");

        // Validar órdenes únicos y que cada número existe
        Set<Integer> ordenes = new HashSet<>();
        for (NumeroEnEspectaculo na : numerosAsignados) {
            validarOrden(na.getOrden());
            if (!ordenes.add(na.getOrden()))
                throw new IllegalArgumentException(
                        "Dos números tienen el mismo orden (" + na.getOrden() + ").");
            numeroRepository.findById(na.getIdNumero())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Número con id " + na.getIdNumero() + " no encontrado."));
        }

        // Crear o recuperar espectáculo
        Espectaculo esp;
        if (idEspExistente != null) {
            esp = espectaculoRepository.findById(idEspExistente)
                    .orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
            esp.getNumerosEnEspectaculo().clear();
            esp.setNombre(nombre.trim());
            esp.setFechaInicio(inicio);
            esp.setFechaFin(fin);
            esp.setCoordinador(coord);
            esp = espectaculoRepository.saveAndFlush(esp);
        } else {
            esp = new Espectaculo(nombre.trim(), inicio, fin, coord);
            esp = espectaculoRepository.save(esp);
        }

        // Crear filas en espectaculo_numero
        for (NumeroEnEspectaculo na : numerosAsignados) {
            Numero num = numeroRepository.findById(na.getIdNumero()).get();
            enRepository.save(new EspectaculoNumero(esp, num, na.getOrden()));
        }

        return esp;
    }

    /**
     * Elimina un espectáculo y sus relaciones con números
     * (las filas de espectaculo_numero se eliminan por CASCADE).
     * Los números en sí NO se eliminan, quedan disponibles en el sistema.
     */
    @Transactional
    public void eliminarEspectaculo(Long idEsp) {
        Espectaculo esp = espectaculoRepository.findById(idEsp)
                .orElseThrow(() -> new IllegalArgumentException("Espectáculo no encontrado."));
        espectaculoRepository.delete(esp);
    }

    // ─── CU5B: Números ────────────────────────────────────────────────

    /** Crea un número nuevo con sus artistas */
    @Transactional
    public Numero crearNumero(String nombre, double duracion, Set<Long> idsArtistas) {
        validarNombreNumero(nombre);
        validarDuracion(duracion);
        if (idsArtistas == null || idsArtistas.isEmpty())
            throw new IllegalArgumentException("El número debe tener al menos 1 artista.");
        if (numeroRepository.existsByNombre(nombre.trim()))
            throw new IllegalArgumentException("Ya existe un número con el nombre: " + nombre.trim());
        Set<Artista> artistas = new HashSet<>(artistaRepository.findAllById(idsArtistas));
        if (artistas.isEmpty())
            throw new IllegalArgumentException("No se encontraron los artistas seleccionados.");
        Numero n = new Numero(nombre.trim(), duracion);
        n.setArtistas(artistas);
        return numeroRepository.save(n);
    }

    /** Modifica nombre, duración y artistas de un número */
    @Transactional
    public Numero modificarNumero(Long idNumero, String nombre, double duracion, Set<Long> idsArtistas) {
        validarNombreNumero(nombre);
        validarDuracion(duracion);
        if (idsArtistas == null || idsArtistas.isEmpty())
            throw new IllegalArgumentException("El número debe tener al menos 1 artista.");
        Numero n = numeroRepository.findById(idNumero)
                .orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));
        // Comprobar nombre único si cambia
        if (!n.getNombre().equalsIgnoreCase(nombre.trim()) &&
                numeroRepository.existsByNombre(nombre.trim()))
            throw new IllegalArgumentException("Ya existe un número con el nombre: " + nombre.trim());
        n.setNombre(nombre.trim());
        n.setDuracion(duracion);
        n.setArtistas(new HashSet<>(artistaRepository.findAllById(idsArtistas)));
        return numeroRepository.save(n);
    }

    /**
     * Elimina un número del sistema.
     * Solo se puede eliminar si no está asignado a ningún espectáculo.
     */
    @Transactional
    public void eliminarNumero(Long idNumero) {
        Numero n = numeroRepository.findById(idNumero)
                .orElseThrow(() -> new IllegalArgumentException("Número no encontrado."));
        if (enRepository.countByNumeroId(idNumero) > 0)
            throw new IllegalArgumentException(
                    "No se puede eliminar el número '" + n.getNombre() +
                    "' porque está asignado a uno o más espectáculos. " +
                    "Quítalo primero de los espectáculos correspondientes.");
        numeroRepository.delete(n);
    }

    // ─── Consultas auxiliares ─────────────────────────────────────────
    public Optional<Numero>   findNumeroById(Long id)              { return numeroRepository.findById(id); }
    public Optional<Numero>   findNumeroByIdConArtistas(Long id)   { return numeroRepository.findByIdConArtistas(id); }
    public int                contarNumerosPorEspectaculo(Long id) { return enRepository.countByEspectaculoId(id); }
    public List<Coordinacion> findAllCoordinadores()               { return coordinacionRepository.findAll(); }
    public List<Artista>      findAllArtistas()                    { return artistaRepository.findAll(); }

    // ─── DTO: número asignado a espectáculo (solo id + orden) ────────
    public static class NumeroEnEspectaculo {
        private final Long idNumero;
        private final int  orden;

        public NumeroEnEspectaculo(Long idNumero, int orden) {
            this.idNumero = idNumero;
            this.orden    = orden;
        }
        public Long getIdNumero() { return idNumero; }
        public int  getOrden()    { return orden; }
    }

    // ─── Validaciones públicas ────────────────────────────────────────
    public void validarNombreNumero(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del número es obligatorio.");
        if (nombre.trim().length() > 100)
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
    }
    public void validarDuracion(double duracion) {
        if (duracion <= 0)
            throw new IllegalArgumentException("La duración debe ser positiva.");
        double dec = duracion - Math.floor(duracion);
        if (Math.abs(dec) > 0.01 && Math.abs(dec - 0.5) > 0.01)
            throw new IllegalArgumentException("La duración solo admite x,0 o x,5 (ej: 3,0 o 2,5).");
    }
    public void validarOrden(int orden) {
        if (orden <= 0) throw new IllegalArgumentException("El orden debe ser ≥ 1.");
    }

    private void validarEspectaculo(String nombre, LocalDate inicio, LocalDate fin, Long idExcluir) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del espectáculo es obligatorio.");
        if (nombre.trim().length() > 25)
            throw new IllegalArgumentException("El nombre no puede superar 25 caracteres.");
        Optional<Espectaculo> ex = espectaculoRepository.findByNombre(nombre.trim());
        if (ex.isPresent() && !ex.get().getId().equals(idExcluir))
            throw new IllegalArgumentException("Ya existe un espectáculo con ese nombre.");
        if (inicio == null) throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        if (fin == null)    throw new IllegalArgumentException("La fecha de fin es obligatoria.");
        if (!fin.isAfter(inicio))
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio.");
        if (ChronoUnit.DAYS.between(inicio, fin) > 365)
            throw new IllegalArgumentException("El periodo no puede superar 1 año.");
    }
}
