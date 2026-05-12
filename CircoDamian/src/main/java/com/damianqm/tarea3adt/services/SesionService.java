package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.Credenciales;
import com.damianqm.tarea3adt.modelo.Perfil;
import com.damianqm.tarea3adt.repositorios.CredencialesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SesionService {

	@Autowired
	private CredencialesRepository credencialesRepository;

	@Value("${usuarioAdmin}")
	private String usuarioAdmin;

	@Value("${passwordAdmin}")
	private String passwordAdmin;

	private Credenciales usuarioActual;
	private boolean sesionAdmin = false;

	public boolean login(String usuario, String password) {
		if (isAutenticado())
			return false;

		String u = usuario.toLowerCase().trim();

		if (u.equals(usuarioAdmin.toLowerCase()) && password.equals(passwordAdmin)) {
			sesionAdmin = true;
			usuarioActual = null;
			return true;
		}

		Optional<Credenciales> cred = credencialesRepository.findByNombreUsuarioAndPassword(u, password);
		cred.ifPresent(c -> usuarioActual = c);
		return cred.isPresent();
	}

	public void logout() {
		usuarioActual = null;
		sesionAdmin = false;
	}

	public boolean isAutenticado() {
		return sesionAdmin || usuarioActual != null;
	}

	public Credenciales getUsuarioActual() {
		return usuarioActual;
	}

	public String getNombreUsuarioActual() {
		if (sesionAdmin)
			return usuarioAdmin;
		if (usuarioActual != null)
			return usuarioActual.getPersona().getNombre();
		return null;
	}

	public Perfil getPerfilActual() {
		if (sesionAdmin)
			return Perfil.ADMIN;
		return usuarioActual != null ? usuarioActual.getPerfil() : null;
	}

	public boolean isAdmin() {
		return sesionAdmin;
	}

	public boolean isArtista() {
		return isAutenticado() && !sesionAdmin && usuarioActual.getPerfil() == Perfil.ARTISTA;
	}

	// El admin puede gestionar espectáculos igual que un coordinador
	public boolean isCoordinacion() {
		if (sesionAdmin)
			return true;
		return isAutenticado() && usuarioActual.getPerfil() == Perfil.COORDINACION;
	}

	public Optional<String> recuperarPassword(String usuario) {
		return credencialesRepository.findByNombreUsuario(usuario.toLowerCase().trim()).map(Credenciales::getPassword);
	}
}
