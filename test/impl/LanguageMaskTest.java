package test.impl;

import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Language;
import test.Test;
import test.TestSettings;
import test.TestState;

@TestSettings(hasArguments = false)
public class LanguageMaskTest extends Test {
	@Override
	public TestState run() {
		try {
			for (int i = 0; i < Math.pow(2, Language.values().length); i++) {
				tryMask(i);
			}
			return TestState.SUCCESS;
		}
		catch (Exception e) {
			message(e);
			return TestState.FATAL_ERROR;
		}
	}

	private void tryMask(int mask) {
		StringBuilder builder = new StringBuilder();
		List<Language> languages = Language.getLanguages(mask);
		for (int i = 0; i < languages.size(); i++) {
			Language language = languages.get(i);
			builder.append(language.languageCode());
			if (i != languages.size() - 1) {
				builder.append(",");
			}
		}
		Seriex.logger().debug("Languages in the mask %s: %s", mask, builder.toString());
	}
}
