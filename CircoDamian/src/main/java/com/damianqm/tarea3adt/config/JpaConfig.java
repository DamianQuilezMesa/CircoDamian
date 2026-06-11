package com.damianqm.tarea3adt.config;

import javax.sql.DataSource;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

// Las entidades de ObjectDB (Incidencia, ResolucionIncidencia) están anotadas
// con @Entity de Jakarta Persistence porque ObjectDB las exige así. Sin embargo,
// Hibernate detectaría esas mismas clases vía @EntityScan y crearía tablas
// MySQL para ellas. Aquí se construye el EntityManagerFactory de MySQL
// filtrando del listado de clases gestionadas todo lo que esté bajo
// el paquete modelo.objectdb, de forma que Hibernate solo cree las tablas
// relacionales del proyecto (Tarea 3) y deje las incidencias para ObjectDB.
@Configuration
public class JpaConfig {

	private static final String MODELO_BASE = "com.damianqm.tarea3adt.modelo";
	private static final String OBJECTDB_PACKAGE = MODELO_BASE + ".objectdb.";

	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
			DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean emf = builder.dataSource(dataSource).packages(MODELO_BASE)
				.persistenceUnit("mysql").build();

		PersistenceUnitPostProcessor excluirObjectDb = pui -> pui.getManagedClassNames()
				.removeIf(name -> name.startsWith(OBJECTDB_PACKAGE));
		emf.setPersistenceUnitPostProcessors(excluirObjectDb);

		return emf;
	}
}
