package test;

import static test.TestState.*;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.SLogger;
import pisi.unitedmeows.seriex.util.collections.GlueList;

public class RunTests {
	private static final List<Test> tests = new GlueList<>();
	private static final Pattern NAME_REPLACE_PATTERN = Pattern.compile("Test", Pattern.LITERAL);

	public static void main(String... args) {
		/****************
		 * Test options *
		 ****************/
		boolean detailed = true;
		/****************
		 * Test options *
		 ****************/
		reflect();
		FormatDetector.registerExtension("seriex", TomlFormat.instance());
		boolean success = true;
		boolean hadWarnings = false;
		Map<TestState, List<Test>> testStates = new EnumMap<>(TestState.class);
		Arrays.stream(TestState.values()).forEach((TestState testState) -> testStates.put(testState, new GlueList<>()));
		SLogger logger = Seriex.logger();
		for (int i = 0; i < tests.size(); i++) {
			Test test = tests.get(i);
			TestState testState = test.run();
			testStates.get(testState).add(test);
			Class<? extends Test> testClass = test.getClass();
			String className = testClass.getSimpleName();
			String testName = NAME_REPLACE_PATTERN.matcher(className).replaceAll("");
			logger.test(detailed, testState, testName, test.message);
			success &= testState != FAIL && testState != FATAL_ERROR;
			if (testState == WARNING && !hadWarnings) {
				hadWarnings = true;
			}
		}
		String seperator = "-----------------";
		if (hadWarnings) {
			logger.debug(seperator);
			logger.debug("There are some warnings you need to check out!");
			logger.debug("Tests with warnings:");
			testStates.get(WARNING).forEach((Test test) -> logger.debug("(WARNING) %s", test.getClass().getSimpleName()));
			logger.debug(seperator);
		}
		if (!success) {
			logger.fatal(seperator);
			logger.fatal("Unsuccessful test run.");
			logger.fatal("Tests with fatal errors / fails:");
			testStates.get(FATAL_ERROR).forEach((Test test) -> logger.fatal("(FATAL ERROR) %s", test.getClass().getSimpleName()));
			testStates.get(FAIL).forEach((Test test) -> logger.fatal("(FAIL) %s", test.getClass().getSimpleName()));
			logger.fatal(seperator);
		} else if (!hadWarnings) {
			logger.info(seperator);
			logger.info("	All tests passed!");
			logger.info(seperator);
		}
		System.exit(hadWarnings ? -1 : success ? 0 : -2); // TODOH exit code depending on if some tests failed / have a warning / etc...
	}

	private static void reflect() {
		Reflections reflections = new Reflections("test.impl", Scanners.SubTypes);
		Set<Class<? extends Test>> testClasses = reflections.getSubTypesOf(Test.class);
		testClasses.forEach(testClass -> {
			try {
				Test test = testClass.newInstance();
				tests.add(test);
			}
			catch (InstantiationException
						| IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
}
