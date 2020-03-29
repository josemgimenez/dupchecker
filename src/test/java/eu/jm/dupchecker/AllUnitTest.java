package eu.jm.dupchecker;

import java.io.File;
import java.util.Stack;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.jm.dupchecker.entity.Archive;
import eu.jm.dupchecker.entity.Directory;
import eu.jm.dupchecker.repository.DatabaseCleanerService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
public class AllUnitTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AllUnitTest.class);

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private DatabaseCleanerService databaseCleanerService;

	private Stack<File> filesToRemove = new Stack<>();

	private static File testWorkDirectory;

	@BeforeAll
	public static void prepareWorkingDir() {
		final File tmpdir = new File(System.getProperty("java.io.tmpdir"));

		final File workingDir = new File(tmpdir, UUID.randomUUID().toString());

		LOGGER.debug("Creating temp working dir {}", workingDir.getAbsolutePath());
		Assertions.assertTrue(workingDir.mkdir(), "Not possible to create temp dir " + workingDir.getAbsolutePath());

		AllUnitTest.testWorkDirectory = workingDir;
	}

	@AfterAll
	public static void removeWorkingDir() {
		LOGGER.debug("Deleting tmp directory {}", testWorkDirectory.getAbsolutePath());
		if (!testWorkDirectory.delete()) {
			LOGGER.warn("Not possible to remove tmp directory {}", testWorkDirectory.getAbsolutePath());
		}
	}

	@AfterEach
	public void cleanDatabase() {
		databaseCleanerService.cleanDatabase();
	}

	@AfterEach
	public void deleteTestFiles() {
		while (!filesToRemove.isEmpty()) {
			File file = filesToRemove.pop();
			LOGGER.debug("Deleting file {}", file.getAbsolutePath());

			if (!file.delete()) {
				LOGGER.warn("Not possible to delete file {}", file.getAbsolutePath());
				file.deleteOnExit();
			}
		}
	}

	protected static File getTestWorkDirectory() {
		return AllUnitTest.testWorkDirectory;
	}

	protected void fullLog() {
		LOGGER.info("Directories");
		em.createQuery("select d from Directory d", Directory.class).getResultList().stream()
				.forEach(d -> LOGGER.info("Directory {}", d));
		LOGGER.info("Archives");
		em.createQuery("select a from Archive a", Archive.class).getResultList().stream()
				.forEach(a -> LOGGER.info("Archive {}", a));
	}

	protected void assertDbContents(int expectedDirectories, int expectedArchives) {
		Assert.assertEquals("Expected directories: " + expectedDirectories, expectedDirectories,
				em.createQuery("select count(*) from Directory", Number.class).getResultList().get(0).intValue());
		Assert.assertEquals("Expected files: " + expectedArchives, expectedArchives,
				em.createQuery("select count(*) from Archive", Number.class).getResultList().get(0).intValue());
	}

	protected void deleteAfterTest(File file) {
		filesToRemove.push(file);
	}

}
