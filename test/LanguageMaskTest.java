package test;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Language;

public class LanguageMaskTest {
	public static void main(String... args) {
		int mask = 0b1 | 0b100;
		Seriex.logger().debug(Language.getLanguage(mask).name());
		Seriex.logger().debug(Language.isLanguageSelected(mask, 0b100) + " # russian");
		Seriex.logger().debug(Language.isLanguageSelected(mask, 0b10) + " # turkish");
		Seriex.logger().debug(Language.isLanguageSelected(mask, 0b1) + " # english");
	}
}
