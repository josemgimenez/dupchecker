package eu.jm.dupchecker;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import eu.jm.dupchecker.service.ScannerService;
import eu.jm.dupchecker.util.HashType;
import eu.jm.dupchecker.util.Options;

@SpringBootApplication
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Autowired
	ScannerService scannerService;

	@Bean
	public ApplicationRunner starter(ApplicationContext ctx) {
		return args -> {
			final List<String> dir = args.getOptionValues("dir");

			if (dir == null || dir.size() != 1) {
				LOGGER.info("No dir found");
				return;
			}

			final File di = new File(dir.iterator().next());

			if (!di.exists()) {
				LOGGER.error("The dir {} does not exist", di.getCanonicalPath());
				return;
			}

			Options o = new Options().setHashType(HashType.PARTIAL);

			Set<Action> optionNames = parseOptions(args.getOptionNames());
			if (optionNames.contains(Action.SCAN)) {
				o.setMaxDepth(999);
				scannerService.scan3(di, o);
			} else if (optionNames.contains(Action.CHECK)) {
				o.setMaxDepth(0);

				scannerService.check(di, o);
			} else if (optionNames.contains(Action.REPORT)) {
				o.setMaxDepth(0);

				scannerService.report();
			}
		};
	}

	private static Set<Action> parseOptions(Set<String> optionNames) {
		if (optionNames == null) {
			return Collections.emptySet();
		}

		Set<Action> actions = new HashSet<>();

		optionNames.stream().forEach(v -> {
			try {
				actions.add(Action.valueOf(v.toUpperCase()));
			} catch (Exception e) {
				// not a big deal
				LOGGER.debug("Option {} not recognised. {}", v, e.getMessage());
			}
		});
		return actions;
	}

	private static enum Action {
		SCAN, CHECK, REPORT;
	}
}
