package pisi.unitedmeows.seriex.util;

import pisi.unitedmeows.yystal.logger.impl.YLogger;

/**
 * basic logger yystal logger with string formatted types
 */
public class BasicLogger extends YLogger {
	public BasicLogger(final Class<?> _clazz) { super(_clazz); }

	public void infof(final String s, final Object... objects) { info(String.format(s, objects)); }

	public void debugf(final String s, final Object... objects) { debug(String.format(s, objects)); }

	public void fatalf(final String s, final Object... objects) { fatal(String.format(s, objects)); }
}
