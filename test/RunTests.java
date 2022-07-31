package test;

import java.util.List;
import java.util.regex.Pattern;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import test.impl.LanguageMaskTest;

public class RunTests {
	private static final List<Test> tests = new GlueList<>();
	private static final Pattern NAME_REPLACE_PATTERN = Pattern.compile("Test", Pattern.LITERAL);

	public static void main(String... args) {
		// TODO reflections
		//		tests.add(new ConfigTest());
		//		tests.add(new DatabaseTest());
		//		tests.add(new HashTest());
		tests.add(new LanguageMaskTest());
		boolean detailed = false;
		FormatDetector.registerExtension("seriex", TomlFormat.instance());
		for (Test test : tests) {
			TestState testState = test.run();
			String testName = NAME_REPLACE_PATTERN.matcher(test.getClass().getSimpleName()).replaceAll("");
			if (testState.isSpecial() && test.message != null) {
				Seriex.logger().test(detailed, testState, testName, test.message);
			} else {
				Seriex.logger().test(detailed, testState, testName);
			}
		}
		System.exit(0); // TODOH exit code depending on if some tests failed / have a warning / etc...
	}
}
