package eu.jm.dupchecker.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eu.jm.dupchecker.entity.Archive;

@Service
@Transactional
public class ArchiveRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryRepository.class);

	@PersistenceContext
	private EntityManager em;

	public void update(Archive k) {
		em.merge(k);
	}

	public void save(Archive k) {
		em.persist(k);
	}

	@Transactional
	public void remove(Archive f) {
		em.remove(f);
	}

	public List<Archive> findDuplicatedFiles(Archive f) {
		LOGGER.trace("Finding duplicate of {}", f);
		List<Archive> r = em.createQuery(
				"select a from Archive a where a.id <> :id and a.partialHash = :partialHash and a.length = :length",
				Archive.class).setParameter("partialHash", f.getPartialHash()).setParameter("id", f.getId())
				.setParameter("length", f.getLength()).getResultList();

		if (StringUtils.isNotBlank(f.getFullHash())) {
			r.removeIf(x -> StringUtils.isNotBlank(x.getFullHash()) && !f.getFullHash().equals(x.getFullHash()));
		}

		LOGGER.trace("Result {}", r);
		return Collections.unmodifiableList(r);
	}

	public Map<String, List<Archive>> findAllDuplicates() {
		List<Archive> r = em.createQuery(
				"select a1 from Archive a1, Archive a2 where a1.id <> a2.id and a1.partialHash = a2.partialHash and a1.length = a2.length"
						+ " and a1.length > 500 "
						+ " and ( a1.fullHash is null or a2.fullHash is null or a1.fullHash = a2.fullHash)",
				Archive.class).getResultList();

		Map<String, List<Archive>> dupes = r.stream().collect(Collectors.groupingBy(Archive::getPartialHash));

		LOGGER.trace("Full duplicated list {}", dupes);
		return dupes;
	}
}
