package com.damianqm.tarea3adt.repositorios.db4o;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.damianqm.tarea3adt.modelo.db4o.LogOperacion;
import com.damianqm.tarea3adt.modelo.db4o.TipoOperacion;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class LogDb4oRepository {

	private static final String DB_PATH = "ficheros/log.db4o";
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public LogDb4oRepository() {
		new File("ficheros").mkdirs();
	}

	private ObjectContainer abrirDb() {
		return Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DB_PATH);
	}

	public void guardar(LogOperacion log) {
		ObjectContainer db = abrirDb();
		try {
			if (log.getId() == null) {
				long siguiente = db.query(LogOperacion.class).size() + 1L;
				log.setId(siguiente);
			}
			db.store(log);
			db.commit();
		} finally {
			db.close();
		}
	}

	// CU10 – Native Query; los campos de LogOperacion son String porque DB4O
	// con Java 21 no puede acceder por reflexión a tipos como Enum o LocalDateTime
	public List<LogOperacion> consultar(String usuario, Set<TipoOperacion> tipos, LocalDateTime desde,
			LocalDateTime hasta) {

		final String u = (usuario != null) ? usuario.trim() : "";
		final String desdeStr = desde != null ? desde.format(FMT) : null;
		final String hastaStr = hasta != null ? hasta.format(FMT) : null;
		final Set<String> tiposStr = tipos != null ? tipos.stream().map(TipoOperacion::name).collect(Collectors.toSet())
				: null;
		final boolean filtrarTipos = tiposStr != null && !tiposStr.isEmpty();

		ObjectContainer db = abrirDb();
		try {
			ObjectSet<LogOperacion> result = db.query(new Predicate<LogOperacion>() {
				@Override
				public boolean match(LogOperacion log) {
					if (!u.isEmpty() && !u.equalsIgnoreCase(log.getUsuario()))
						return false;
					if (filtrarTipos && !tiposStr.contains(log.getTipoOperacion()))
						return false;
					if (desdeStr != null && log.getFechaHoraStr() != null
							&& log.getFechaHoraStr().compareTo(desdeStr) < 0)
						return false;
					if (hastaStr != null && log.getFechaHoraStr() != null
							&& log.getFechaHoraStr().compareTo(hastaStr) > 0)
						return false;
					return true;
				}
			});
			return new ArrayList<>(result);
		} finally {
			db.close();
		}
	}

	public List<String> obtenerUsuariosDistintos() {
		ObjectContainer db = abrirDb();
		try {
			return db.query(LogOperacion.class).stream().map(LogOperacion::getUsuario)
					.filter(u -> u != null && !u.isBlank()).distinct().sorted().toList();
		} finally {
			db.close();
		}
	}
}
