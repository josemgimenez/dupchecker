package eu.jm.dupchecker;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class MainTest extends AllUnitTest {
	@Autowired
	private ApplicationRunner main;

	@Test
	public void testScanExistingDir() throws Exception {
		main.run(createArguments(getTestWorkDirectory().getAbsolutePath()));

		assertDbContents(1, 0);
	}

	@Test
	public void testScanNonExistingDir() throws Exception {
		main.run(createArguments(
				getTestWorkDirectory().getAbsolutePath() + File.separator + UUID.randomUUID().toString()));

		assertDbContents(0, 0);
	}

	private static ApplicationArguments createArguments(String dir) {
		return new ApplicationArguments() {

			@Override
			public String[] getSourceArgs() {
				return null;
			}

			@Override
			public List<String> getOptionValues(String name) {
				if (name.equals("dir")) {
					return Arrays.asList(dir);
				}
				return null;
			}

			@Override
			public Set<String> getOptionNames() {
				return null;
			}

			@Override
			public List<String> getNonOptionArgs() {
				return null;
			}

			@Override
			public boolean containsOption(String name) {
				return false;
			}
		};

	}
}
