package pisi.unitedmeows.seriex.util.logging;

import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class SLogger extends YLogger {
	public SLogger(Class<?> _clazz) {
		super(_clazz);
		setPrefix("Seriex");
		// idk if this will work update yystal but...
		/*
		 * update:
		 * doesnt work
		 * Exception in thread "main" java.lang.NoSuchMethodError: org.fusesource.jansi.AnsiConsole.isInstalled()Z
				at pisi.unitedmeows.yystal.logger.impl.YLogger.setColored(YLogger.java:215)
				at pisi.unitedmeows.seriex.util.logging.SLogger.<init>(SLogger.java:10)
				at pisi.unitedmeows.seriex.Seriex.<init>(Seriex.java:44)
				at pisi.unitedmeows.seriex.Seriex.main(Seriex.java:130)
		 */
		setColored(false);
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
