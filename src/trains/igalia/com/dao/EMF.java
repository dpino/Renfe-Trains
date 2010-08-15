package trains.igalia.com.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import trains.igalia.com.model.Train;

public final class EMF {

	private static final EntityManagerFactory emfInstance = Persistence
			.createEntityManagerFactory("transactions-optional");

	private EMF() {

	}

	public static EntityManagerFactory get() {
		return emfInstance;
	}

	public static void persist(Train train) {

		EntityManager em = get().createEntityManager();
		try {
			em.persist(train);
		} finally {
			em.close();
		}

	}

}