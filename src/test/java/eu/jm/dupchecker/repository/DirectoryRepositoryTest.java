package eu.jm.dupchecker.repository;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.jm.dupchecker.AllUnitTest;
import eu.jm.dupchecker.entity.Directory;

public class DirectoryRepositoryTest extends AllUnitTest {
//	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryRepositoryTest.class);
	@Autowired
	private DirectoryRepository directoryRepository;

	@Test
	public void testGetDirectory() throws IOException {
		File workingDir = getTestWorkDirectory();

		File testDir = new File(workingDir, UUID.randomUUID().toString());
		deleteAfterTest(testDir);

		String canonicalPath = testDir.getCanonicalPath();
		Directory inDb = directoryRepository.getDirectory(canonicalPath);

		Assertions.assertNull(inDb, "Directory " + canonicalPath + " should not be in the DB");

		// Creation
		Directory newDir = directoryRepository.getOrCreateDirectory(canonicalPath);

		Assertions.assertNotNull(newDir, "Directory " + canonicalPath + " should be in the DB");

		// Retrieval
		Directory newDir2 = directoryRepository.getOrCreateDirectory(canonicalPath);
		Assertions.assertEquals(newDir, newDir2, "It has to be the same directory");
	}
}
