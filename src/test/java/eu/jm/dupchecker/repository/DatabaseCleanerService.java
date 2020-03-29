package eu.jm.dupchecker.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.jm.dupchecker.entity.Archive;
import eu.jm.dupchecker.entity.Directory;

@Transactional
@Service
public class DatabaseCleanerService {
	@PersistenceContext
	private EntityManager em;

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseCleanerService.class);

	public void cleanDatabase() {
		LOGGER.info("Removing directories");

		em.createQuery("select d from Directory d", Directory.class).getResultList().stream()
				.forEach(d -> em.remove(d));

		LOGGER.info("Removing archives");

		em.createQuery("select a from Archive a", Archive.class).getResultList().stream().forEach(a -> em.remove(a));
	}

}
