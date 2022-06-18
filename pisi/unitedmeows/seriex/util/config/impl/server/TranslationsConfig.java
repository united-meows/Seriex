package pisi.unitedmeows.seriex.util.config.impl.server;

import static com.electronwill.nightconfig.core.CommentedConfig.*;

import java.io.File;
import java.util.Arrays;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.yystal.utils.Pair;

// TODO didnt finish yet
@Cfg(name = "Translations" , manual = true , multi = true)
public class TranslationsConfig extends Config {
	public TranslationsConfig(File toWrite, String extension, Language... languages) {
		super("Translations", true, ConfigType.MULTIPLE, toWrite);
		Arrays.stream(languages).forEach(language -> {
			File file = new File(String.format("%s/%s%s", toWrite, language.languageCode(), extension));
			configs.put(language.languageCode(), new Pair<>(file, inMemoryConcurrent()));
		});
		this.manual = true;
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
