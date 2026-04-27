package com.damianqm.tarea3adt.modelo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clase usada como clave primaria compuesta de EspectaculoNumero. Los nombres
 * de los campos deben coincidir con los @Id de la entidad.
 */
public class EspectaculoNumeroId implements Serializable {

	private Long espectaculo;
	private Long numero;

	public EspectaculoNumeroId() {
	}

	public EspectaculoNumeroId(Long espectaculo, Long numero) {
		this.espectaculo = espectaculo;
		this.numero = numero;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EspectaculoNumeroId))
			return false;
		EspectaculoNumeroId that = (EspectaculoNumeroId) o;
		return Objects.equals(espectaculo, that.espectaculo) && Objects.equals(numero, that.numero);
	}

	@Override
	public int hashCode() {
		return Objects.hash(espectaculo, numero);
	}
}
