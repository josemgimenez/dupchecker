package eu.jm.dupchecker.repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.jm.dupchecker.entity.Directory;

@Repository
public class DirectoryRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryRepository.class);

	@PersistenceContext
	private EntityManager em;

	@Transactional
	public Directory getOrCreateDirectory(final String canonicalPath) {
		final Directory existing = getDirectory(canonicalPath);

		if (existing != null) {
			return existing;
		}

		LOGGER.debug("Creating dir {}", canonicalPath);
		Directory d = new Directory();
		d.setCanonicalPath(canonicalPath);

		persist(d);
		return d;
	}

	public void persist(Directory d) {
		em.persist(d);
		em.flush();
	}

	@Transactional(readOnly = true)
	public Directory getDirectory(final String canonicalPath) {
		LOGGER.debug("Processing dir {}", canonicalPath);

		try {
			Directory directory = em.createQuery("from Directory d where d.canonicalPath = :fp", Directory.class)
					.setParameter("fp", canonicalPath).getSingleResult();

			LOGGER.debug("Dir found {}", directory);
			return directory;

		} catch (NoResultException nre) {
			// ignore
			LOGGER.debug("Dir not found {}", canonicalPath);
		}
		return null;
	}
}
