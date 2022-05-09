package pisi.unitedmeows.seriex.util.logging;

import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class SLogger extends YLogger {
	public SLogger(Class<?> _clazz) {
		super(_clazz);
		setPrefix("Seriex");
		// idk if this will work update yystal but...
		setColored(true);
	}

	public void info(String message, Object... args) {
		info(String.format(message, args));
	}

	public void warn(String message, Object... args) {
		warn(String.format(message, args));
	}

	public void fatal(String message, Object... args) {
		fatal(String.format(message, args));
	}

	public void debug(String message, Object... args) {
		debug(String.format(message, args));
	}
}
