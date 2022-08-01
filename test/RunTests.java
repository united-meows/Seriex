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
		boolean success = true;
		for (int i = 0; i < tests.size(); i++) {
			Test test = tests.get(i);
			TestState testState = test.run();
			Class<? extends Test> testClass = test.getClass();
			String className = testClass.getSimpleName();
			String testName = NAME_REPLACE_PATTERN.matcher(className).replaceAll("");
			Seriex.logger().test(detailed, testState, testName, test.message);
		}
		System.exit(0); // TODOH exit code depending on if some tests failed / have a warning / etc...
	}
}
