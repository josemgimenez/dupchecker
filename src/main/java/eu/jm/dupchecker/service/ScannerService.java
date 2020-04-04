package eu.jm.dupchecker.service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.jm.dupchecker.entity.Archive;
import eu.jm.dupchecker.entity.Directory;
import eu.jm.dupchecker.repository.ArchiveRepository;
import eu.jm.dupchecker.util.HashData;
import eu.jm.dupchecker.util.HashType;
import eu.jm.dupchecker.util.HashUtils;
import eu.jm.dupchecker.util.Options;

@Service
@Transactional
public class ScannerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScannerService.class);

	@Autowired
	private DirectoryService directoryService;

	@Autowired
	private ArchiveRepository archiveRepository;

	private void add(final File f, final Directory parent, HashType hashType) {
		LOGGER.info("Processing file {}", f.getName());
		HashData hd = HashUtils.getHashData(f, hashType);

		Archive k = new Archive();

		k.setDirectory(parent);
		k.setName(f.getName());
		k.setLength(f.length());
		k.setLastModified(f.lastModified());

		k.setPartialHash(hd.getPartialHash());
		k.setFullHash(hd.getFullHash());
		archiveRepository.save(k);
	}

	public void scan3(File dir, Options options) {
		scan3(dir, options, options.getMaxDepth());
	}

	private void scan3(File dir, Options options, int maxDepth) {
		Assert.notNull(dir, "dir can't be null");
		Assert.isTrue(dir.isDirectory(),
				"Only directories can be scanned and " + dir.getAbsolutePath() + " is not a directory");

		LOGGER.info("Processing dir {}", dir);
		Directory d = directoryService.getOrCreateDirectory(dir);

		if (d == null) {
			return;
		}

		File[] listFiles = dir.listFiles();
		if (listFiles == null || listFiles.length == 0) {
			LOGGER.debug("No files found in {}", dir.getAbsolutePath());
			return;
		}

		Map<String, File> filesMap = Arrays.asList(listFiles).stream().collect(Collectors.toMap(File::getName, f -> f));

		Set<Archive> newArchives = new HashSet<Archive>();

		Set<Archive> archives = d.getFiles();
		archives.stream().forEach(f -> {
			String fileName = f.getName();
			if (filesMap.containsKey(fileName)) {
				File file = filesMap.get(fileName);
				archiveRepository.processFile(f, file, options.getHashType());

				newArchives.add(f);

				filesMap.remove(fileName);
			} else {
				archiveRepository.remove(f);
			}
		});
		d.setFiles(newArchives);
		directoryService.persist(d);

		filesMap.values().stream().filter(f -> f.isFile() && f.canRead())
				.forEach(f -> add(f, d, options.getHashType()));

		if (maxDepth > 0) {
			filesMap.values().stream().filter(f -> f.isDirectory() && f.canRead())
					.forEach(f -> scan3(f, options, maxDepth - 1));
		}
	}

	public TreeMap<Archive, List<Archive>> check(File dir, Options options) {
		scan3(dir, options, 0);

		Directory d = directoryService.getOrCreateDirectory(dir);

		TreeMap<Archive, List<Archive>> duplicates = new TreeMap<Archive, List<Archive>>(new Comparator<Archive>() {

			@Override
			public int compare(Archive o1, Archive o2) {
				return o2.getLength().compareTo(o1.getLength());
			}
		});

		// Map<Archive, List<Archive>> duplicates = new HashMap<>();

		d.getFiles().stream().forEach(f -> {
			List<Archive> duplicated = archiveRepository.findDuplicatedFiles(f);
			if (!duplicated.isEmpty()) {
				duplicates.put(f, duplicated);
			}
		});

		duplicates.keySet().stream().forEach(k -> {
			LOGGER.info("File name: {} size: {} id {}", k.getName(), k.getLength(), k.getId());

			duplicates.get(k).stream().forEach(dd -> {
				LOGGER.info("\t{}/{} id {}", dd.getDirectory().getCanonicalPath(), dd.getName(), dd.getId());
			});
		});

		return duplicates;
	}

	public Map<String, List<Archive>> report() {
		Map<String, List<Archive>> duplicates = archiveRepository.findAllDuplicates();

		int group = 1;
		for (List<Archive> list : duplicates.values()) {
			LOGGER.info("Group {} - size {}", group, list.get(0).getLength());

			for (Archive a : list) {
				LOGGER.info("\t{}/{}", a.getDirectory().getCanonicalPath(), a.getName());
			}
		}
		return duplicates;
	}
}
