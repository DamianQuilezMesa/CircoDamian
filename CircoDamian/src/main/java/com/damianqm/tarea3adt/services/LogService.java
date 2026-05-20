package com.damianqm.tarea3adt.services;

import com.damianqm.tarea3adt.modelo.db4o.LogOperacion;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import com.damianqm.tarea3adt.repositorios.db4o.LogDb4oRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class LogService {

	@Autowired
	private LogDb4oRepository logRepo;

	// CU7 – guarda un log en DB4O cada vez que ocurre un alta/modificación/borrado
	public void registrarOperacion(String usuario, TipoOperacion tipoOperacion, String resumen) {
		LogOperacion log = new LogOperacion(usuario, tipoOperacion, resumen);
		logRepo.guardar(log);
	}

	// CU10 – consulta el historial de un usuario con filtros opcionales de tipo y
	// fecha
	public List<LogOperacion> consultarHistorial(String usuario, Set<TipoOperacion> tipos, LocalDateTime desde,
			LocalDateTime hasta) {
		if (usuario == null || usuario.isBlank()) {
			throw new IllegalArgumentException("Debe indicar un nombre de usuario para consultar el historial.");
		}
		List<LogOperacion> resultado = logRepo.consultar(usuario, tipos, desde, hasta);
		// Ordenar por fecha descendente
		resultado.sort((a, b) -> {
			if (a.getFechaHora() == null && b.getFechaHora() == null)
				return 0;
			if (a.getFechaHora() == null)
				return 1;
			if (b.getFechaHora() == null)
				return -1;
			return b.getFechaHora().compareTo(a.getFechaHora());
		});
		return resultado;
	}

	public List<String> obtenerUsuariosConLog() {
		return logRepo.obtenerUsuariosDistintos();
	}
}
