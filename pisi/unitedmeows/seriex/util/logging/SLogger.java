package pisi.unitedmeows.seriex.util.logging;

import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class SLogger extends YLogger {
	public SLogger(Class<?> _clazz) {
		super(_clazz);
		setPrefix("Seriex");
		//	setColored(true); we cant do this for now, Spigot already has JAnsi and we try to use a newer one so it clashes
		// waiting for yystal update sadly
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
