package test;

import java.util.Arrays;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Language;

public class LanguageUnicodeTest {
	public static void main(String... args) {
		Arrays.stream(Language.values()).forEach(language -> {
			String seperator = "-------------------";
			Seriex.logger().debug(language.name());
			Seriex.logger().debug(language.languageCode());
			Seriex.logger().debug(language.unicode());
			Seriex.logger().debug(seperator);
		});
	}
}
