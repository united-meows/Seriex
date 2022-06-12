package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import java.util.Map;
import java.util.WeakHashMap;

import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class I18n implements ICleanup {
	private Map<String, String> cache = new WeakHashMap<>();

	public String getString(String message, PlayerW player) {
		FileManager fileManager = get().fileManager();
		Config config = fileManager.getConfig(TRANSLATIONS);
		return cache.computeIfAbsent(message, (String msg) -> config.getValue(msg, config.config));
	}

	@Override
	public void cleanup() throws SeriexException {
		cache.clear();
	}
}
