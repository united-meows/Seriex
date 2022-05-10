package pisi.unitedmeows.seriex.util.translation;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;

import pisi.unitedmeows.seriex.util.config.FileManager;

public class I18n {
	private I18n() {}

	public static String getString(String message) {
		FileManager fileManager = get().fileManager();
		return fileManager.getConfig(TRANSLATIONS).getValue(message);
	}
}
