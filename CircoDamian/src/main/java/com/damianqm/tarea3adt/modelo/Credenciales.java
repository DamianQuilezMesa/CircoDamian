package com.damianqm.tarea3adt.modelo;

import jakarta.persistence.*;

/** Credenciales de acceso de una persona. Relación 1:1 con Persona. */
@Entity
@Table(name = "credenciales")
public class Credenciales {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre_usuario", unique = true, nullable = false)
	private String nombreUsuario;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Perfil perfil;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_persona", nullable = false, unique = true)
	private Persona persona;

	public Credenciales() {
	}

	public Credenciales(String nombreUsuario, String password, Perfil perfil, Persona persona) {
		this.nombreUsuario = nombreUsuario;
		this.password = password;
		this.perfil = perfil;
		this.persona = persona;
	}

	public Long getId() {
		return id;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public Persona getPersona() {
		return persona;
	}

	public void setPersona(Persona persona) {
		this.persona = persona;
	}
}
