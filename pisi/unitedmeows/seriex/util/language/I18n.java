package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import com.electronwill.nightconfig.core.CommentedConfig;

import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.utils.Pair;

public class I18n implements ICleanup {
	private Map<String, String> cache = new WeakHashMap<>();

	public String getString(String message, PlayerW player) {
		FileManager fileManager = get().fileManager();
		TranslationsConfig config = (TranslationsConfig) fileManager.getConfig(TRANSLATIONS);
		return cache.computeIfAbsent(message, (String msg) -> {
			Language language = player.selectedLanguage();
			String languageCode = language.languageCode();
			Pair<File, CommentedConfig> pair = config.getConfigs().get(languageCode);
			return config.getValue(msg, pair.item2());
		});
	}

	@Override
	public void cleanup() throws SeriexException {
		cache.clear();
	}
}
