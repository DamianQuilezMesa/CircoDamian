package com.damianqm.tarea3adt.repositorios;

import com.damianqm.tarea3adt.modelo.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    boolean existsByEmail(String email);
}
