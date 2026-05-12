package com.damianqm.tarea3adt.config;

import com.damianqm.tarea3adt.modelo.*;
import com.damianqm.tarea3adt.repositorios.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Set;

@Component
public class DatabaseInitializer implements ApplicationRunner {

	private final DataSource dataSource;
	private final PersonaRepository personaRepo;
	private final ArtistaRepository artistaRepo;
	private final CoordinacionRepository coordRepo;
	private final CredencialesRepository credRepo;
	private final NumeroRepository numeroRepo;
	private final EspectaculoRepository espectaculoRepo;

	public DatabaseInitializer(DataSource dataSource, PersonaRepository personaRepo, ArtistaRepository artistaRepo,
			CoordinacionRepository coordRepo, CredencialesRepository credRepo, NumeroRepository numeroRepo,
			EspectaculoRepository espectaculoRepo) {
		this.dataSource = dataSource;
		this.personaRepo = personaRepo;
		this.artistaRepo = artistaRepo;
		this.coordRepo = coordRepo;
		this.credRepo = credRepo;
		this.numeroRepo = numeroRepo;
		this.espectaculoRepo = espectaculoRepo;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		crearTablasSiNoExisten();
		if (personaRepo.count() > 0)
			return;
		insertarDatosMuestra();
	}

	private void crearTablasSiNoExisten() throws Exception {
		try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {

			st.execute("CREATE TABLE IF NOT EXISTS persona (" + "id BIGINT NOT NULL AUTO_INCREMENT, "
					+ "nombre VARCHAR(255) NOT NULL, " + "email VARCHAR(255) NOT NULL, "
					+ "nacionalidad VARCHAR(255) NOT NULL, " + "PRIMARY KEY (id), "
					+ "CONSTRAINT uk_persona_email UNIQUE (email)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS artista (" + "id_persona BIGINT NOT NULL, " + "apodo VARCHAR(255), "
					+ "PRIMARY KEY (id_persona), "
					+ "CONSTRAINT fk_artista_persona FOREIGN KEY (id_persona) REFERENCES persona (id)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS artista_especialidad (" + "id_artista BIGINT NOT NULL, "
					+ "especialidad VARCHAR(255) NOT NULL, "
					+ "CONSTRAINT uk_artista_esp UNIQUE (id_artista, especialidad), "
					+ "CONSTRAINT fk_artesp_artista FOREIGN KEY (id_artista) REFERENCES artista (id_persona)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS coordinacion (" + "id_persona BIGINT NOT NULL, "
					+ "senior BOOLEAN NOT NULL DEFAULT FALSE, " + "fecha_senior DATE, " + "PRIMARY KEY (id_persona), "
					+ "CONSTRAINT fk_coord_persona FOREIGN KEY (id_persona) REFERENCES persona (id)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS credenciales (" + "id BIGINT NOT NULL AUTO_INCREMENT, "
					+ "nombre_usuario VARCHAR(255) NOT NULL, " + "password VARCHAR(255) NOT NULL, "
					+ "perfil VARCHAR(50) NOT NULL, " + "id_persona BIGINT NOT NULL, " + "PRIMARY KEY (id), "
					+ "CONSTRAINT uk_cred_usuario UNIQUE (nombre_usuario), "
					+ "CONSTRAINT uk_cred_persona UNIQUE (id_persona), "
					+ "CONSTRAINT fk_cred_persona FOREIGN KEY (id_persona) REFERENCES persona (id)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS espectaculo (" + "id BIGINT NOT NULL AUTO_INCREMENT, "
					+ "nombre VARCHAR(25) NOT NULL, " + "fecha_inicio DATE NOT NULL, " + "fecha_fin DATE NOT NULL, "
					+ "id_coordinador BIGINT NOT NULL, " + "PRIMARY KEY (id), "
					+ "CONSTRAINT uk_esp_nombre UNIQUE (nombre), "
					+ "CONSTRAINT fk_esp_coord FOREIGN KEY (id_coordinador) REFERENCES coordinacion (id_persona)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS numero (" + "id BIGINT NOT NULL AUTO_INCREMENT, "
					+ "nombre VARCHAR(100) NOT NULL, " + "duracion DOUBLE NOT NULL, " + "orden INT NOT NULL, "
					+ "id_espectaculo BIGINT NOT NULL, " + "PRIMARY KEY (id), "
					+ "CONSTRAINT uq_num_esp_orden UNIQUE (id_espectaculo, orden), "
					+ "CONSTRAINT fk_numero_espectaculo FOREIGN KEY (id_espectaculo) REFERENCES espectaculo (id)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

			st.execute("CREATE TABLE IF NOT EXISTS numero_artista (" + "id_numero BIGINT NOT NULL, "
					+ "id_artista BIGINT NOT NULL, " + "PRIMARY KEY (id_numero, id_artista), "
					+ "CONSTRAINT fk_numart_numero FOREIGN KEY (id_numero) REFERENCES numero (id), "
					+ "CONSTRAINT fk_numart_artista FOREIGN KEY (id_artista) REFERENCES artista (id_persona)"
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
		}
	}

	@Transactional
	public void insertarDatosMuestra() {
		Coordinacion laura = coordRepo.save(
				new Coordinacion("Laura Mendez", "laura.mendez@circo.com", "ES", true, LocalDate.of(2020, 3, 15)));
		Coordinacion carlos = coordRepo
				.save(new Coordinacion("Carlos Ruiz", "carlos.ruiz@circo.com", "MX", false, null));

		Artista marco = artistaRepo.save(new Artista("Marco Rossi", "marco.rossi@circo.com", "IT", "El Gran Marco",
				Set.of(Especialidad.ACROBACIA, Especialidad.EQUILIBRISMO)));
		Artista sofia = artistaRepo.save(
				new Artista("Sofia Dupont", "sofia.dupont@circo.com", "FR", "La Pluma", Set.of(Especialidad.HUMOR)));
		Artista ivan = artistaRepo.save(new Artista("Ivan Petrov", "ivan.petrov@circo.com", "RU", null,
				Set.of(Especialidad.ACROBACIA, Especialidad.MALABARISMO)));
		Artista ana = artistaRepo.save(new Artista("Ana Lima", "ana.lima@circo.com", "BR", "Aninha",
				Set.of(Especialidad.MAGIA, Especialidad.HUMOR)));
		Artista kenji = artistaRepo.save(new Artista("Kenji Tanaka", "kenji.tanaka@circo.com", "JP", "Kenjiro",
				Set.of(Especialidad.EQUILIBRISMO, Especialidad.ACROBACIA)));
		Artista elena = artistaRepo.save(
				new Artista("Elena Varga", "elena.varga@circo.com", "HU", null, Set.of(Especialidad.MALABARISMO)));
		Artista luis = artistaRepo.save(new Artista("Luis Torres", "luis.torres@circo.com", "MO", "El Mago Torres",
				Set.of(Especialidad.MAGIA)));

		credRepo.save(new Credenciales("laura", "laura123", Perfil.COORDINACION, laura));
		credRepo.save(new Credenciales("carlos", "carlos123", Perfil.COORDINACION, carlos));
		credRepo.save(new Credenciales("marco", "marco123", Perfil.ARTISTA, marco));
		credRepo.save(new Credenciales("sofia", "sofia123", Perfil.ARTISTA, sofia));
		credRepo.save(new Credenciales("ivan", "ivan123", Perfil.ARTISTA, ivan));
		credRepo.save(new Credenciales("ana", "ana123", Perfil.ARTISTA, ana));
		credRepo.save(new Credenciales("kenji", "kenji123", Perfil.ARTISTA, kenji));
		credRepo.save(new Credenciales("elena", "elena123", Perfil.ARTISTA, elena));
		credRepo.save(new Credenciales("luis", "luis123", Perfil.ARTISTA, luis));

		Espectaculo e1 = espectaculoRepo.save(
				new Espectaculo("Noche de Estrellas", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), laura));
		Espectaculo e2 = espectaculoRepo
				.save(new Espectaculo("El Gran Finale", LocalDate.of(2025, 9, 15), LocalDate.of(2025, 12, 15), laura));
		Espectaculo e3 = espectaculoRepo.save(
				new Espectaculo("Magia y Acrobacia", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10), carlos));

		Numero n1 = new Numero("Apertura Acrobatica", 10.0, 1, e1);
		Numero n2 = new Numero("El Mago del Fuego", 8.5, 2, e1);
		Numero n3 = new Numero("Equilibrio en Altura", 12.0, 3, e1);
		Numero n4 = new Numero("Cierre con Humor", 6.5, 4, e1);
		Numero n5 = new Numero("Vuelo Libre", 15.0, 1, e2);
		Numero n6 = new Numero("Juegos Malabares", 9.0, 2, e2);
		Numero n7 = new Numero("Fuego y Fantasia", 20.0, 3, e2);
		Numero n8 = new Numero("Ilusiones", 10.5, 1, e3);
		Numero n9 = new Numero("Torre Humana", 14.0, 2, e3);
		Numero n10 = new Numero("Desaparicion Final", 8.0, 3, e3);

		n1.setArtistas(Set.of(marco, ivan));
		n2.setArtistas(Set.of(luis));
		n3.setArtistas(Set.of(kenji, marco));
		n4.setArtistas(Set.of(sofia, ana));
		n5.setArtistas(Set.of(marco, kenji));
		n6.setArtistas(Set.of(ivan, elena));
		n7.setArtistas(Set.of(marco, sofia, ivan, luis));
		n8.setArtistas(Set.of(luis, ana));
		n9.setArtistas(Set.of(marco, ivan, kenji));
		n10.setArtistas(Set.of(luis, sofia));

		numeroRepo.saveAll(java.util.List.of(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10));
	}
}
