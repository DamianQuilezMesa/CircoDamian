package com.damianqm.tarea3adt.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// Configuración de MongoDB leída desde application.properties.
// @EnableMongoRepositories registra sólo el paquete de repositorios MongoDB,
// evitando que Spring Data mezcle los repositorios JPA con los de Mongo.
@Configuration
@EnableMongoRepositories(basePackages = "com.damianqm.tarea3adt.repositorios.mongodb")
public class MongoConfig {

	@Value("${mongodb.host}")
	private String host;

	@Value("${mongodb.port}")
	private int port;

	@Value("${mongodb.database}")
	private String database;

	@Bean
	public MongoClient mongoClient() {
		return MongoClients.create("mongodb://" + host + ":" + port);
	}

	@Bean
	public MongoTemplate mongoTemplate() {
		return new MongoTemplate(mongoClient(), database);
	}
}
