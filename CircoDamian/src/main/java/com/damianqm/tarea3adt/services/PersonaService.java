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

@Service
public class PersonaService {

    @Autowired private PersonaRepository personaRepository;
    @Autowired private CredencialesRepository credencialesRepository;
    @Autowired private ArtistaRepository artistaRepository;
    @Autowired private CoordinacionRepository coordinacionRepository;
    @Autowired private PaisesLoader paisesLoader;

    @Transactional
    public Artista registrarArtista(String nombre, String email, String nacionalidad,
                                    String apodo, Set<Especialidad> especialidades,
                                    String nombreUsuario, String password) {
        validarNombre(nombre);
        validarEmail(email);
        validarNacionalidad(nacionalidad);
        validarEspecialidades(especialidades);
        validarPersonaNueva(email, nombreUsuario, password);
        Artista artista = new Artista(nombre.trim(), email.trim().toLowerCase(),
                nacionalidad.trim().toUpperCase(), apodo, especialidades);
        artista = artistaRepository.save(artista);
        credencialesRepository.save(new Credenciales(
                nombreUsuario.toLowerCase().trim(), password, Perfil.ARTISTA, artista));
        return artista;
    }

    @Transactional
    public Coordinacion registrarCoordinacion(String nombre, String email, String nacionalidad,
                                              boolean senior, LocalDate fechaSenior,
                                              String nombreUsuario, String password) {
        validarNombre(nombre);
        validarEmail(email);
        validarNacionalidad(nacionalidad);
        validarPersonaNueva(email, nombreUsuario, password);
        if (senior && fechaSenior == null)
            throw new IllegalArgumentException("Si es senior, debe indicar la fecha desde cuándo.");
        if (senior && fechaSenior.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La fecha senior no puede ser futura.");
        Coordinacion coord = new Coordinacion(nombre.trim(), email.trim().toLowerCase(),
                nacionalidad.trim().toUpperCase(), senior, fechaSenior);
        coord = coordinacionRepository.save(coord);
        credencialesRepository.save(new Credenciales(
                nombreUsuario.toLowerCase().trim(), password, Perfil.COORDINACION, coord));
        return coord;
    }

    @Transactional
    public Persona modificarDatosPersonales(Long idPersona, String nombre,
                                            String email, String nacionalidad) {
        Persona p = personaRepository.findById(idPersona)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + idPersona));
        validarNombre(nombre);
        validarEmail(email);
        validarNacionalidad(nacionalidad);
        if (!p.getEmail().equalsIgnoreCase(email.trim()) &&
                personaRepository.existsByEmail(email.trim().toLowerCase()))
            throw new IllegalArgumentException("El email ya está en uso: " + email);
        p.setNombre(nombre.trim());
        p.setEmail(email.trim().toLowerCase());
        p.setNacionalidad(nacionalidad.trim().toUpperCase());
        return personaRepository.save(p);
    }

    @Transactional
    public Artista modificarDatosArtista(Long idArtista, String apodo,
                                         Set<Especialidad> especialidades) {
        Artista a = artistaRepository.findById(idArtista)
                .orElseThrow(() -> new IllegalArgumentException("Artista no encontrado: " + idArtista));
        validarEspecialidades(especialidades);
        a.setApodo(apodo != null && !apodo.isBlank() ? apodo.trim() : null);
        a.setEspecialidades(especialidades);
        return artistaRepository.save(a);
    }

    @Transactional
    public Coordinacion modificarDatosCoordinacion(Long idCoord, boolean senior,
                                                   LocalDate fechaSenior) {
        Coordinacion c = coordinacionRepository.findById(idCoord)
                .orElseThrow(() -> new IllegalArgumentException("Coordinación no encontrada: " + idCoord));
        if (senior && fechaSenior == null)
            throw new IllegalArgumentException("Si es senior, debe indicar la fecha desde cuándo.");
        if (senior && fechaSenior.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La fecha senior no puede ser futura.");
        c.setSenior(senior);
        c.setFechaSenior(senior ? fechaSenior : null);
        return coordinacionRepository.save(c);
    }

    public List<Persona>      findAllPersonas()      { return personaRepository.findAll(); }
    public List<Artista>      findAllArtistas()      { return artistaRepository.findAll(); }
    public List<Coordinacion> findAllCoordinadores() { return coordinacionRepository.findAll(); }
    public Optional<Persona>      findPersonaById(Long id)      { return personaRepository.findById(id); }
    public Optional<Artista>      findArtistaById(Long id)      { return artistaRepository.findById(id); }
    /** Carga artista con sus números y espectáculos (evita LazyInitializationException en FichaArtista). */
    public Optional<Artista>      findArtistaConTrayectoria(Long id) { return artistaRepository.findByIdConNumeros(id); }
    public Optional<Coordinacion> findCoordinacionById(Long id) { return coordinacionRepository.findById(id); }
    public Optional<Credenciales> findCredencialesByPersonaId(Long idPersona) {
        return credencialesRepository.findByPersonaId(idPersona);
    }

    // ─── Validaciones ─────────────────────────────────────────────────

    /** Nombre: obligatorio, 2-100 chars, solo letras/espacios/guiones/apóstrofes */
    public void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        String n = nombre.trim();
        if (n.length() < 2)
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres.");
        if (n.length() > 100)
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
        if (!n.matches("[\\p{L}\\s'\\-]+"))
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, espacios, guiones y apóstrofes.");
    }

    /** Email: formato estándar xxx@xxx.xx */
    public void validarEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio.");
        if (email.trim().length() > 150)
            throw new IllegalArgumentException("El email no puede superar 150 caracteres.");
        if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException(
                    "El email no tiene un formato válido (ejemplo: nombre@dominio.com).");
    }

    /** Nacionalidad: código ISO del XML de países */
    public void validarNacionalidad(String nacionalidad) {
        if (nacionalidad == null || nacionalidad.isBlank())
            throw new IllegalArgumentException("La nacionalidad es obligatoria.");
        if (!paisesLoader.esCodidoValido(nacionalidad.trim()))
            throw new IllegalArgumentException(
                    "Nacionalidad no válida. Selecciona un país de la lista desplegable.");
    }

    private void validarPersonaNueva(String email, String nombreUsuario, String password) {
        if (personaRepository.existsByEmail(email.trim().toLowerCase()))
            throw new IllegalArgumentException("El email ya está registrado: " + email);
        String usuario = nombreUsuario == null ? "" : nombreUsuario.toLowerCase().trim();
        if (usuario.isEmpty())
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        if (!usuario.matches("[a-z]{3,}"))
            throw new IllegalArgumentException(
                    "El usuario solo puede contener letras minúsculas sin tildes ni espacios, y debe tener más de 2 caracteres.");
        if (credencialesRepository.existsByNombreUsuario(usuario))
            throw new IllegalArgumentException("El nombre de usuario ya existe: " + usuario);
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        if (password.contains(" "))
            throw new IllegalArgumentException("La contraseña no puede contener espacios.");
        if (password.length() <= 2)
            throw new IllegalArgumentException("La contraseña debe tener más de 2 caracteres.");
    }

    private void validarEspecialidades(Set<Especialidad> especialidades) {
        if (especialidades == null || especialidades.isEmpty())
            throw new IllegalArgumentException("El artista debe tener al menos una especialidad.");
        if (especialidades.size() > 5)
            throw new IllegalArgumentException("El artista no puede tener más de 5 especialidades.");
    }
}
