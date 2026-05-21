package com.damianqm.tarea3adt.repositorios.mongodb;

import com.damianqm.tarea3adt.modelo.mongodb.Dossier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositorio Spring Data MongoDB para la colección "dossiers".
// La conexión y la base de datos se configuran en MongoConfig a partir
// de las propiedades mongodb.* de application.properties.
@Repository
public interface DossierRepository extends MongoRepository<Dossier, String> {

	Optional<Dossier> findByIdArtista(Long idArtista);
}
