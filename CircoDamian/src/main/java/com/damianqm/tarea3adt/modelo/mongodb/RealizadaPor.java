package com.damianqm.tarea3adt.modelo.mongodb;

// Documento embebido que identifica a la persona que emite una evaluación
// (identificador lógico de la persona + su rol/perfil en el sistema).
public class RealizadaPor {

	private Long idPersona;
	private String rol;

	public RealizadaPor() {
	}

	public RealizadaPor(Long idPersona, String rol) {
		this.idPersona = idPersona;
		this.rol = rol;
	}

	public Long getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Long idPersona) {
		this.idPersona = idPersona;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}
}
