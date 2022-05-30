package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import java.util.HashMap;
import java.util.Map;

import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class I18n implements ICleanup {
	private Map<String, String> cache = new HashMap<>();

	public String getString(String message) {
		FileManager fileManager = get().fileManager();
		Config config = fileManager.getConfig(TRANSLATIONS);
		return cache.computeIfAbsent(message, (String msg) -> config.getValue(msg, config.config));
	}

	@Override
	public void cleanup() throws SeriexException {
		cache.clear();
	}
}
