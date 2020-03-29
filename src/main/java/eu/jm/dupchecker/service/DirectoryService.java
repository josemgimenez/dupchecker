package eu.jm.dupchecker.service;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.jm.dupchecker.entity.Directory;
import eu.jm.dupchecker.repository.DirectoryRepository;

@Service
@Transactional
public class DirectoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryService.class);

	@Autowired
	private DirectoryRepository directoryRepository;

	public Directory getOrCreateDirectory(final File directory) {
		final String canonicalPath;
		try {
			canonicalPath = directory.getCanonicalPath();
		} catch (IOException e) {
			LOGGER.warn("Not possible to get canonical path of {}", directory);
			return null;
		}

		return directoryRepository.getOrCreateDirectory(canonicalPath);
	}

	public void persist(Directory d) {
		directoryRepository.persist(d);
	}
}
