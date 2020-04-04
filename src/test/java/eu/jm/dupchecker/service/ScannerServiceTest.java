package eu.jm.dupchecker.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.jm.dupchecker.AllUnitTest;
import eu.jm.dupchecker.entity.Archive;
import eu.jm.dupchecker.util.HashType;
import eu.jm.dupchecker.util.HashUtils;
import eu.jm.dupchecker.util.Options;

public class ScannerServiceTest extends AllUnitTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScannerServiceTest.class);

	@Autowired
	private ScannerService scannerService;

	@Test
	public void testGetDirectory() throws IOException {
		File workingDir = getTestWorkDirectory();
		Options o = new Options().setMaxDepth(1);

		File f1 = createFile(workingDir, "a.txt");
		deleteAfterTest(f1);

		assertDbContents(0, 0);
		scannerService.scan3(workingDir, o);

		assertDbContents(1, 1);

		File f2 = createFile(workingDir, "b.txt");
		deleteAfterTest(f2);
		scannerService.scan3(workingDir, o);

		assertDbContents(1, 2);

		File f3 = createFile(workingDir, "c.txt");
		deleteAfterTest(f3);
		scannerService.scan3(workingDir, o);
		assertDbContents(1, 3);
	}

	@Test
	public void testUpdate() throws IOException, InterruptedException {
		Options o = new Options().setMaxDepth(1);

		File workingDir = getTestWorkDirectory();
		assertDbContents(0, 0);

		File f1 = createFile(workingDir, "a.txt");
		deleteAfterTest(f1);

		scannerService.scan3(workingDir, o);
		assertDbContents(1, 1);

		Archive a1 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);

		f1 = createFile(workingDir, "a.txt");
		// Precision is of 1 second, so instead of waiting we just move the time a few
		// seconds to the past
		f1.setLastModified(System.currentTimeMillis() - 10000);

		scannerService.scan3(workingDir, o);
		assertDbContents(1, 1);
		Archive a2 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);

		LOGGER.info("a1 {} - a2 {}", a1, a2);
		assertNotEquals(a1.getPartialHash(), a2.getPartialHash(), "The partial hash has to be different");

		assertDbContents(1, 1);
	}

	@Test
	public void testFileMillis() throws IOException {
		Options o = new Options().setMaxDepth(1);

		File workingDir = getTestWorkDirectory();
		assertDbContents(0, 0);

		File f1 = createFile(workingDir, "a.txt");
		deleteAfterTest(f1);

		scannerService.scan3(workingDir, o);

		Archive a1 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);

		assertEquals(f1.lastModified(), a1.getLastModified());

		em.detach(a1);
		em.clear();
		a1 = null;

		Archive a2 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);
		assertTrue(a1 != a2);
		assertEquals(f1.lastModified(), a2.getLastModified());
	}

	@Test
	public void testDelete() throws IOException {
		Options o = new Options().setMaxDepth(1);
		File workingDir = getTestWorkDirectory();

		File f1 = createFile(workingDir, "a.txt");
		deleteAfterTest(f1);

		assertDbContents(0, 0);
		scannerService.scan3(workingDir, o);

		assertDbContents(1, 1);

		File f2 = createFile(workingDir, "b.txt");
		scannerService.scan3(workingDir, o);

		assertDbContents(1, 2);

		Assert.assertTrue("Not possible to remove temp file " + f2.getAbsolutePath(), f2.delete());
		scannerService.scan3(workingDir, o);
		assertDbContents(1, 1);
	}

	@Test
	public void testMultipleDir() throws IOException {
		Options o = new Options().setMaxDepth(0);
		File workingDir = getTestWorkDirectory();

		File subDir = new File(workingDir, "newdir");
		subDir.mkdir();
		deleteAfterTest(subDir);

		File f1 = createFile(subDir, "a.txt");
		deleteAfterTest(f1);

		assertDbContents(0, 0);
		scannerService.scan3(workingDir, o);

		// no files scanned and the same
		assertDbContents(1, 0);

		Options o2 = new Options().setMaxDepth(2);
		scannerService.scan3(workingDir, o2);

		assertDbContents(2, 1);
	}

	@Test
	public void testFullHash() throws IOException {
		Options oPartial = new Options().setMaxDepth(0).setHashType(HashType.PARTIAL);
		File workingDir = getTestWorkDirectory();

		File f1 = createFile(workingDir, "a.txt", HashUtils.PARTIAL_HASH_BYTES);
		deleteAfterTest(f1);

		assertDbContents(0, 0);
		scannerService.scan3(workingDir, oPartial);

		// no files scanned and the same
		assertDbContents(1, 1);

		Archive a1 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);

		Assert.assertNotNull("Partial hash has to be present", a1.getPartialHash());
		Assert.assertNull("Full hash has to be absent", a1.getFullHash());
		Assert.assertEquals(f1.lastModified(), a1.getLastModified().longValue());

		Options oFull = new Options().setMaxDepth(0).setHashType(HashType.FULL);
		scannerService.scan3(workingDir, oFull);
		assertDbContents(1, 1);

		Archive a2 = em.createQuery("select a from Archive a", Archive.class).getResultList().get(0);

		Assert.assertEquals("Partial hash has to be the same", a1.getPartialHash(), a2.getPartialHash());
		Assert.assertNotNull("Partial hash has to be present", a2.getPartialHash());
		Assert.assertNotNull("Full hash not found", a2.getFullHash());
	}

	@Test
	public void testCheck() throws IOException {
		Options oPartial = new Options().setMaxDepth(1).setHashType(HashType.PARTIAL);
		File workingDir = getTestWorkDirectory();

		File f1 = createFile(workingDir, "a.txt", HashUtils.PARTIAL_HASH_BYTES);
		File f2 = createFile(workingDir, "b.txt", HashUtils.PARTIAL_HASH_BYTES);
		File f3 = createFile(workingDir, "c.txt", HashUtils.PARTIAL_HASH_BYTES);

		deleteAfterTest(f1);
		deleteAfterTest(f2);
		deleteAfterTest(f3);

		File subdir = new File(workingDir, UUID.randomUUID().toString());
		Assert.assertTrue("Not possible to create dir " + subdir.getAbsolutePath(), subdir.mkdir());

		deleteAfterTest(subdir);

		File af1 = new File(subdir, "a-dupe.txt");
		deleteAfterTest(af1);

		Files.copy(f1.toPath(), af1.toPath());
		File f4 = createFile(subdir, "b.txt", HashUtils.PARTIAL_HASH_BYTES);
		File f5 = createFile(subdir, "c.txt", HashUtils.PARTIAL_HASH_BYTES);
		deleteAfterTest(f4);
		deleteAfterTest(f5);

		scannerService.scan3(workingDir, oPartial);
		TreeMap<Archive, List<Archive>> result = scannerService.check(workingDir, oPartial);

		Assert.assertEquals("Expected only one duplicated file", 1, result.size());

		Archive dup = result.keySet().iterator().next();
		Assert.assertEquals("Filename does not match", "a.txt", dup.getName());

		List<Archive> duplis = result.get(dup);
		Assert.assertEquals("Expected only one duplicated file", 1, duplis.size());
		Assert.assertEquals("Filename does not match", "a-dupe.txt", duplis.get(0).getName());
	}

	// ------------------------------------------------------------------------

	private static File createFile(File workingDir, String fileName) throws IOException {
		return createFile(workingDir, fileName, 20);
	}

	private static File createFile(File workingDir, String fileName, int count) throws IOException {
		final String padding = RandomStringUtils.randomAlphabetic(count);

		File f1 = new File(workingDir, fileName);
		FileUtils.writeStringToFile(f1, fileName + "\t" + padding, Charset.defaultCharset());

		return f1;
	}
}
