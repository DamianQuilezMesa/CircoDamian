package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.repositorios.*;
import com.damianqm.tarea3adt.util.PaisesLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Servicio de personas (CU3): registro, modificación y consultas. */
@Service
public class PersonaService {

    @Autowired private PersonaRepository      personaRepository;
    @Autowired private CredencialesRepository credencialesRepository;
    @Autowired private ArtistaRepository      artistaRepository;
    @Autowired private CoordinacionRepository coordinacionRepository;
    @Autowired private PaisesLoader           paisesLoader;

    // ─── Registro ─────────────────────────────────────────────────────

    @Transactional
    public Artista registrarArtista(String nombre, String email, String nacionalidad,
                                    String apodo, Set<Especialidad> especialidades,
                                    String usuario, String password) {
        validarPersona(nombre, email, nacionalidad);
        validarEspecialidades(especialidades);
        validarCredencialesNuevas(email, usuario, password);

        Artista a = new Artista(nombre.trim(), email.trim().toLowerCase(),
                nacionalidad.trim().toUpperCase(), apodo, especialidades);
        a = artistaRepository.save(a);
        credencialesRepository.save(new Credenciales(
                usuario.toLowerCase().trim(), password, Perfil.ARTISTA, a));
        return a;
    }

    @Transactional
    public Coordinacion registrarCoordinacion(String nombre, String email, String nacionalidad,
                                              boolean senior, LocalDate fechaSenior,
                                              String usuario, String password) {
        validarPersona(nombre, email, nacionalidad);
        validarCredencialesNuevas(email, usuario, password);
        validarSenior(senior, fechaSenior);

        Coordinacion c = new Coordinacion(nombre.trim(), email.trim().toLowerCase(),
                nacionalidad.trim().toUpperCase(), senior, fechaSenior);
        c = coordinacionRepository.save(c);
        credencialesRepository.save(new Credenciales(
                usuario.toLowerCase().trim(), password, Perfil.COORDINACION, c));
        return c;
    }

    // ─── Modificación ─────────────────────────────────────────────────

    @Transactional
    public Persona modificarDatosPersonales(Long id, String nombre, String email, String nacionalidad) {
        Persona p = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada."));
        validarPersona(nombre, email, nacionalidad);
        if (!p.getEmail().equalsIgnoreCase(email.trim()) &&
            personaRepository.existsByEmail(email.trim().toLowerCase()))
            throw new IllegalArgumentException("El email ya está en uso.");

        p.setNombre(nombre.trim());
        p.setEmail(email.trim().toLowerCase());
        p.setNacionalidad(nacionalidad.trim().toUpperCase());
        return personaRepository.save(p);
    }

    @Transactional
    public Artista modificarDatosArtista(Long id, String apodo, Set<Especialidad> especialidades) {
        Artista a = artistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artista no encontrado."));
        validarEspecialidades(especialidades);
        a.setApodo(apodo != null && !apodo.isBlank() ? apodo.trim() : null);
        a.setEspecialidades(especialidades);
        return artistaRepository.save(a);
    }

    @Transactional
    public Coordinacion modificarDatosCoordinacion(Long id, boolean senior, LocalDate fechaSenior) {
        Coordinacion c = coordinacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coordinación no encontrada."));
        validarSenior(senior, fechaSenior);
        c.setSenior(senior);
        c.setFechaSenior(senior ? fechaSenior : null);
        return coordinacionRepository.save(c);
    }

    // ─── Consultas ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Persona> findAllPersonas() { return personaRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Artista> findAllArtistas() { return artistaRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Coordinacion> findAllCoordinadores() { return coordinacionRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Artista> findArtistaById(Long id) { return artistaRepository.findById(id); }

    @Transactional(readOnly = true)
    public Optional<Artista> findArtistaConTrayectoria(Long id) {
        return artistaRepository.findByIdConNumeros(id);
    }

    @Transactional(readOnly = true)
    public Optional<Coordinacion> findCoordinacionById(Long id) {
        return coordinacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Credenciales> findCredencialesByPersonaId(Long id) {
        return credencialesRepository.findByPersonaId(id);
    }

    // ─── Validaciones ─────────────────────────────────────────────────

    private void validarPersona(String nombre, String email, String nacionalidad) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        String n = nombre.trim();
        if (n.length() < 2 || n.length() > 100)
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 100 caracteres.");
        if (!n.matches("[\\p{L}\\s'\\-]+"))
            throw new IllegalArgumentException("El nombre solo puede contener letras, espacios, guiones y apóstrofes.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");
        if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("El email no tiene formato válido.");
        if (nacionalidad == null || !paisesLoader.esCodigoValido(nacionalidad.trim()))
            throw new IllegalArgumentException("Selecciona una nacionalidad válida de la lista.");
    }

    private void validarCredencialesNuevas(String email, String usuario, String password) {
        if (personaRepository.existsByEmail(email.trim().toLowerCase()))
            throw new IllegalArgumentException("El email ya está registrado.");
        String u = usuario == null ? "" : usuario.toLowerCase().trim();
        if (!u.matches("[a-z]{3,}"))
            throw new IllegalArgumentException(
                "El usuario debe tener más de 2 letras minúsculas sin tildes ni espacios.");
        if (credencialesRepository.existsByNombreUsuario(u))
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        if (password == null || password.isBlank() || password.contains(" ") || password.length() <= 2)
            throw new IllegalArgumentException(
                "La contraseña debe tener más de 2 caracteres y no contener espacios.");
    }

    private void validarEspecialidades(Set<Especialidad> especialidades) {
        if (especialidades == null || especialidades.isEmpty())
            throw new IllegalArgumentException("El artista debe tener al menos una especialidad.");
    }

    private void validarSenior(boolean senior, LocalDate fecha) {
        if (senior && fecha == null)
            throw new IllegalArgumentException("Si es senior, debe indicar la fecha.");
        if (senior && fecha.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La fecha senior no puede ser futura.");
    }
}
