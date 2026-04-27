package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.Credenciales;
import com.damianqm.tarea3adt.modelo.Perfil;
import com.damianqm.tarea3adt.repositorios.CredencialesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Gestiona la sesión del usuario activo. Singleton de Spring, estado
 * compartido.
 */
@Service
public class SesionService {

	@Autowired
	private CredencialesRepository credencialesRepository;

	private Credenciales usuarioActual;

	public boolean login(String usuario, String password) {
		if (usuarioActual != null)
			return false;
		Optional<Credenciales> cred = credencialesRepository
				.findByNombreUsuarioAndPassword(usuario.toLowerCase().trim(), password);
		cred.ifPresent(c -> usuarioActual = c);
		return cred.isPresent();
	}

	public void logout() {
		usuarioActual = null;
	}

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

	public boolean isArtista() {
		return isAutenticado() && usuarioActual.getPerfil() == Perfil.ARTISTA;
	}

	/** Admin también puede gestionar como Coordinación. */
	public boolean isCoordinacion() {
		return isAutenticado()
				&& (usuarioActual.getPerfil() == Perfil.COORDINACION || usuarioActual.getPerfil() == Perfil.ADMIN);
	}

	public Optional<String> recuperarPassword(String usuario) {
		return credencialesRepository.findByNombreUsuario(usuario.toLowerCase().trim()).map(Credenciales::getPassword);
	}
}
