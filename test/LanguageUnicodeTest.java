package test;

import java.util.Arrays;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Languages;

public class LanguageUnicodeTest {
	public static void main(String... args) {
		Arrays.stream(Languages.values()).forEach(language -> {
			String seperator = "-------------------";
			Seriex.logger().debug(seperator);
			Seriex.logger().debug(language.name());
			Seriex.logger().debug(seperator.substring((int) (seperator.length() / 2D)));
			Seriex.logger().debug(language.languageCode());
			Seriex.logger().debug(language.unicode());
			Seriex.logger().debug(seperator);
		});
	}
}
