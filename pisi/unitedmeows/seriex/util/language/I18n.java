package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import com.electronwill.nightconfig.core.CommentedConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.utils.Pair;

public class I18n implements ICleanup {
	// todo ycache / caffeine
	private Map<String, String> cache = new WeakHashMap<>(100, 0.5F);

	public String getString(String message, PlayerW player) {
		try {
			FileManager fileManager = get().fileManager();
			TranslationsConfig config = (TranslationsConfig) fileManager.getConfig(TRANSLATIONS);
			return cache.computeIfAbsent(message, (String msg) -> {
				Language language = player.selectedLanguage();
				String languageCode = language.languageCode();
				Pair<File, CommentedConfig> pair = config.getConfigs().get(languageCode);
				String value = config.getValue(msg, pair.item2());
				if (value == null) { // missing key
					Seriex.logger().fatal("Translation key %s for language %s is missing!", message, player.selectedLanguage().languageCode());
					return msg;
				}
				return value;
			});
		}
		catch (Exception e) { // config / filemanager issue
			e.printStackTrace();
			Seriex.logger().fatal("Translation API is broken?");
			return message;
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		cache.clear();
	}
}
