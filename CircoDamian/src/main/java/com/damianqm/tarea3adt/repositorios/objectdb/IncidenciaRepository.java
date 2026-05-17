package com.damianqm.tarea3adt.repositorios.objectdb;

import com.damianqm.tarea3adt.modelo.objectdb.Incidencia;
import com.damianqm.tarea3adt.modelo.objectdb.ResolucionIncidencia;
import com.damianqm.tarea3adt.modelo.objectdb.TipoIncidencia;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para incidencias persistidas en ObjectDB (modo embebido).
 * Gestiona su propio EntityManagerFactory, independiente del de MySQL.
 */
@Repository
public class IncidenciaRepository {

	private final EntityManagerFactory emf;

	public IncidenciaRepository(@Value("${objectdb.url}") String url) {
		// En modo embebido la URL es suficiente: objectdb:db/circo_incidencias.odb
		this.emf = Persistence.createEntityManagerFactory(url);
	}

	// CU8 – Guardar nueva incidencia
	public Incidencia guardar(Incidencia incidencia) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(incidencia);
			em.getTransaction().commit();
			return incidencia;
		} catch (Exception e) {
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();
			throw new RuntimeException("Error al guardar la incidencia: " + e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	// CU9 – Resolver incidencia: marca como resuelta y persiste la resolución
	// (transaccional)
	public void resolver(Long idIncidencia, ResolucionIncidencia resolucion) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			Incidencia inc = em.find(Incidencia.class, idIncidencia);
			if (inc == null)
				throw new IllegalArgumentException("Incidencia no encontrada con id " + idIncidencia);
			if (inc.isResuelta())
				throw new IllegalStateException("La incidencia ya está resuelta.");

			inc.setResuelta(true);
			em.merge(inc);
			resolucion.setIncidencia(inc);
			em.persist(resolucion);
			em.getTransaction().commit();
		} catch (Exception e) {
			if (em.getTransaction().isActive())
				em.getTransaction().rollback();
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	// CU11 – Consultar con filtros JPQL dinámico
	public List<Incidencia> consultar(TipoIncidencia tipo, Boolean resuelta, Long idEspectaculo, Long idNumero,
			LocalDateTime desde, LocalDateTime hasta) {

		EntityManager em = emf.createEntityManager();
		try {
			StringBuilder jpql = new StringBuilder("SELECT i FROM Incidencia i WHERE 1=1");
			if (tipo != null)
				jpql.append(" AND i.tipo = :tipo");
			if (resuelta != null)
				jpql.append(" AND i.resuelta = :resuelta");
			if (idEspectaculo != null)
				jpql.append(" AND i.idEspectaculo = :idEsp");
			if (idNumero != null)
				jpql.append(" AND i.idNumero = :idNum");
			if (desde != null)
				jpql.append(" AND i.fechaHora >= :desde");
			if (hasta != null)
				jpql.append(" AND i.fechaHora <= :hasta");
			jpql.append(" ORDER BY i.fechaHora DESC");

			TypedQuery<Incidencia> query = em.createQuery(jpql.toString(), Incidencia.class);
			if (tipo != null)
				query.setParameter("tipo", tipo);
			if (resuelta != null)
				query.setParameter("resuelta", resuelta);
			if (idEspectaculo != null)
				query.setParameter("idEsp", idEspectaculo);
			if (idNumero != null)
				query.setParameter("idNum", idNumero);
			if (desde != null)
				query.setParameter("desde", desde);
			if (hasta != null)
				query.setParameter("hasta", hasta);

			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public Optional<Incidencia> findById(Long id) {
		EntityManager em = emf.createEntityManager();
		try {
			return Optional.ofNullable(em.find(Incidencia.class, id));
		} finally {
			em.close();
		}
	}

	public List<Incidencia> findAll() {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery("SELECT i FROM Incidencia i ORDER BY i.fechaHora DESC", Incidencia.class)
					.getResultList();
		} finally {
			em.close();
		}
	}

	@PreDestroy
	public void cerrar() {
		if (emf != null && emf.isOpen())
			emf.close();
	}
}
