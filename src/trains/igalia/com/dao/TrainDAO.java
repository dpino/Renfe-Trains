package trains.igalia.com.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.joda.time.LocalDate;

import trains.igalia.com.model.Train;

public class TrainDAO {

	public static Train find(String originCode, String destinationCode,
			LocalDate date) {
		EntityManager em = EMF.get().createEntityManager();

		Query query = em
				.createQuery("SELECT b FROM Train b WHERE originCode = :originCode "
						+ "AND destinationCode = :destinationCode AND date = :date");

		query.setParameter("originCode", originCode);
		query.setParameter("destinationCode", destinationCode);
		query.setParameter("date", toDate(date));

		return (Train) getSingleResult(query);
	}

	private static Object getSingleResult(Query query) {
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private static Date toDate(LocalDate date) {
		return date.toDateTimeAtStartOfDay().toDate();
	}

}