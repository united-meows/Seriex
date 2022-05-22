package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.Config;

public class I18n {
	private I18n() {}

	public static String getString(String message) {
		FileManager fileManager = get().fileManager();
		Config config = fileManager.getConfig(TRANSLATIONS);
		return config.getValue(message, config.config);
	}
}
