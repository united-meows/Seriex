package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.get;

import java.util.Map;
import java.util.WeakHashMap;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.multi.impl.TranslationsConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class I18n implements ICleanup {
	private final Map<String, String> cache = new WeakHashMap<>(100, 0.5F);

	public String getMessage(Messages message, PlayerW playerW, Object... arguments) {
		return String.format(getMessage(message, playerW), arguments);
	}

	public String getMessage(Messages message, PlayerW playerW) {
		return getString(message.cfgString, message.defaultValue, playerW);
	}

	public String getMessage(Messages message, Language language) {
		return getString(message.cfgString, message.defaultValue, language);
	}

	public String getMessage(Messages message, Language language, Object... arguments) {
		return String.format(getString(message.cfgString, message.defaultValue, language), arguments);
	}

	private String getString(String message, String defaultValue, Language language) {
		try {
			FileManager fileManager = get().fileManager();
			TranslationsConfig config = fileManager.config(TranslationsConfig.class);
			return cache.computeIfAbsent(message, (String msg) -> {
				String languageCode = language.languageCode();
				String value = config.get(languageCode, msg);
				if (value == null) {
					Seriex.get().logger().error("Translation key {} for language {} is missing!", message, languageCode);
					return defaultValue;
				}
				return value;
			});
		}
		catch (Exception e) { // config / filemanager issue
			e.printStackTrace();
			Seriex.get().logger().error("Translation API is broken?");
			return message;
		}
	}

	private String getString(String message, String defaultValue, PlayerW player) {
		Language language = Language.ENGLISH;
		if (player != null)
			language = player.selectedLanguage();
		return getString(message, defaultValue, language);
	}

	@Override
	public void cleanup() throws SeriexException {
		cache.clear();
	}
}
