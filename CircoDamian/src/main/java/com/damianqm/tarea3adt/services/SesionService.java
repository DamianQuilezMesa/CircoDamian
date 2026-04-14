package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.Credenciales;
import com.damianqm.tarea3adt.modelo.Perfil;
import com.damianqm.tarea3adt.repositorios.CredencialesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Gestiona la sesión activa de la aplicación (CU2: Login/Logout).
 * Implementa patrón Singleton de sesión a través de Spring (@Service).
 */
@Service
public class SesionService {

    @Autowired
    private CredencialesRepository credencialesRepository;

    /** Usuario autenticado actualmente. null = Invitado. */
    private Credenciales usuarioActual = null;

    // ─── Login ────────────────────────────────────────────────────────

    /**
     * Intenta autenticar con usuario y contraseña.
     * @return true si las credenciales son correctas y no hay sesión activa.
     */
    public boolean login(String nombreUsuario, String password) {
        if (usuarioActual != null) return false; // ya hay sesión activa
        Optional<Credenciales> cred = credencialesRepository
                .findByNombreUsuarioAndPassword(nombreUsuario.toLowerCase().trim(), password);
        if (cred.isPresent()) {
            usuarioActual = cred.get();
            return true;
        }
        return false;
    }

    /** Cierra la sesión activa, volviendo al perfil Invitado. */
    public void logout() {
        usuarioActual = null;
    }

    // ─── Consultas de sesión ──────────────────────────────────────────

    public boolean isAutenticado() {
        return usuarioActual != null;
    }

    public Credenciales getUsuarioActual() {
        return usuarioActual;
    }

    public Perfil getPerfilActual() {
        return usuarioActual != null ? usuarioActual.getPerfil() : null;
    }

    public boolean isAdmin() {
        return isAutenticado() && usuarioActual.getPerfil() == Perfil.ADMIN;
    }

    public boolean isCoordinacion() {
        return isAutenticado() && (usuarioActual.getPerfil() == Perfil.COORDINACION
                || usuarioActual.getPerfil() == Perfil.ADMIN);
    }

    public boolean isArtista() {
        return isAutenticado() && usuarioActual.getPerfil() == Perfil.ARTISTA;
    }

    /**
     * Recupera la contraseña de un usuario por nombre de usuario.
     * (CU2 – recuperar contraseña: en producción se enviaría por email)
     */
    public Optional<String> recuperarPassword(String nombreUsuario) {
        return credencialesRepository.findByNombreUsuario(nombreUsuario.toLowerCase().trim())
                .map(Credenciales::getPassword);
    }
}
