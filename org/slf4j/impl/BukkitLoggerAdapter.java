/*
 * This entire file is sublicensed to you under GPLv3 or (at your option) any
 * later version. The original copyright notice is retained below.
 */
/*
 * Portions of this file are
 * Copyright (C) 2016-2017 Ronald Jack Jenkins Jr., SLF4Bukkit contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Copyright (c) 2004-2012 QOS.ch All rights reserved.
 * Permission is hereby granted, free  of charge, to any person obtaining a  copy  of this  software  and  associated  documentation files  (the "Software"), to  deal in  the Software without  restriction, including without limitation  the rights to
 * use, copy, modify,  merge, publish, distribute,  sublicense, and/or sell  copies of  the Software,  and to permit persons to whom the Software  is furnished to do so, subject to the following conditions:
 * The  above  copyright  notice  and  this permission  notice  shall  be included in all copies or substantial portions of the Software.
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND, EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.impl;

import info.ronjenkins.slf4bukkit.ColorMapper;
import info.ronjenkins.slf4bukkit.ColorMapperFactory;
import info.ronjenkins.slf4bukkit.ColorMarker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableMap;

/**
 * <p>
 * A merger of SLF4J's {@code SimpleLogger} and {@code JDK14LoggerAdapter},
 * wired to log all messages to the enclosing Bukkit plugin. The plugin is
 * identified by reading the "name" attribute from {@code plugin.yml} in the
 * current classloader.
 * </p>
 *
 * <p>
 * Plugins that include SLF4Bukkit can use the following values in
 * {@code config.yml} to configure the behavior of SLF4Bukkit. SLF4Bukkit uses
 * Bukkit's plugin configuration API to retrieve config values, so both on-disk
 * and built-in {@code config.yml} behavior is supported.
 * </p>
 *
 * <ul>
 * <li>{@code slf4j.defaultLogLevel} - Default log level for all SLF4Bukkit
 * loggers in this plugin. Must be one of "trace", "debug", "info", "warn", or
 * "error" (case-insensitive). If unspecified or given any other value, defaults
 * to "info".</li>
 *
 * <li>{@code slf4j.showHeader} -Set to {@code true} if you want to output the
 * {@code [SLF4J]} header. If unspecified or given any other value, defaults to
 * {@code false}.</li>
 *
 * <li>{@code slf4j.showLogName} - Set to {@code true} if you want the logger
 * instance name (wrapped in curly braces) to be included in output messages. If
 * unspecified or given any other value, defaults to {@code false}. If this
 * option is {@code true}, it overrides {@code slf4j.showShortLogName}.</li>
 *
 * <li>{@code slf4j.showShortLogName} - Set to {@code true} if you want the
 * logger instance's short name (wrapped in curly braces) to be included in
 * output messages. The short name is equal to the full name with every
 * dot-separated portion of the full name (except the last portion) truncated to
 * its first character. If unspecified or given any other value, defaults to
 * {@code true}. This option is ignored if {@code slf4j.showLogName} is
 * {@code true}.</li>
 *
 * <li>{@code slf4j.showThreadName} -Set to {@code true} if you want to output
 * the current thread name, wrapped in brackets. If unspecified or given any
 * other value, defaults to {@code false}.</li>
 *
 * <li>{@code slf4j.colors.LEVEL} - Default color for all messages of this
 * level. Possible values come from SLF4Bukkit's {@link ColorMarker} values.
 * Both keys and values in this section are treated as case-insensitive. Invalid
 * values for either the key or value of an entry result in that entry being
 * ignored. Default values are: error=RED, warn=YELLOW, others=NONE. When used
 * programmatically via methods in this class, {@link ColorMarker}s always
 * override these config values.</li>
 *
 * <li>{@code slf4j.log.<em>a.b.c</em>} - Logging detail level for an SLF4Bukkit
 * logger instance in this plugin named "a.b.c". Right-side value must be one of
 * "trace", "debug", "info", "warn", or "error" (case-insensitive). When a
 * logger named "a.b.c" is initialized, its level is assigned from this
 * property. If unspecified or given any other value, the level of the nearest
 * parent logger will be used. If no parent logger level is set, then the value
 * specified by {@code slf4j.defaultLogLevel} for this plugin will be used.</li>
 * </ul>
 *
 * <p>
 * SLF4J messages at level {@code TRACE} or {@code DEBUG} are logged to Bukkit
 * at level {@code INFO} because Bukkit does not enable any levels higher than
 * {@code INFO}. Therefore, only SLF4J messages at level {@code TRACE} or
 * {@code DEBUG} show their SLF4J level in the message that is logged to the
 * server console.
 * </p>
 *
 * <p>
 * Because SLF4Bukkit's configuration comes from the plugin configuration,
 * SLF4Bukkit supports configuration reloading. To achieve this, call
 * {@link #init(boolean)} with argument {@code true} after calling
 * {@link Plugin#reloadConfig()}.
 * </p>
 *
 * <p>
 * It is possible for SLF4J loggers to be used before the plugin is registered
 * with Bukkit's plugin manager. SLF4Bukkit is considered to be
 * <i>uninitialized</i> as long as the plugin cannot be retrieved from Bukkit's
 * plugin manager. While in the uninitialized state, SLF4Bukkit:
 * </p>
 *
 * <ul>
 * <li>uses {@link Bukkit#getLogger()} instead of {@link Plugin#getLogger()}.</li>
 * <li>uses the default configuration values (see above).</li>
 * <li>attempts to initialize itself upon every logging call until the plugin is
 * retrievable from Bukkit's plugin manager, at which point SLF4Bukkit is
 * considered to be <i>initialized</i>. Once initialized,
 * {@link Plugin#getLogger()} and {@link Plugin#getConfig() the plugin YAML
 * configuration values} are used.</li>
 * </ul>
 *
 * <p>
 * For this reason, it is strongly recommended that you not emit any log
 * messages via SLF4Bukkit until your plugin's {@link Plugin#onLoad() onLoad()}
 * method has begun execution. (You can safely log messages inside the
 * {@code onLoad()} method, because your plugin is registered by that time.)
 * Logging inside static initializers, the plugin class constructor and other
 * pre-plugin-registration areas of your code is discouraged.
 * </p>
 *
 * <p>
 * With no configuration, the default output includes the logger short name and
 * the message, followed by the line separator for the host.
 * </p>
 *
 * <p>
 * This logger supports only {@link ColorMarker}s, which are used to format the
 * logged message and throwable. All other marker types are ignored. The usage
 * of markers does not affect whether or not a given logging level is enabled.
 * </p>
 *
 * <p>
 * When executed on a Bukkit implementation that does not contain the JAnsi
 * library (e.g. PaperSpigot), all color-related functionality is silently
 * ignored. Any messages logged in such an environment by SLF4Bukkit will have
 * any {@link ChatColor} values stripped. SLF4Bukkit does not emit any warnings
 * when executed in an environment where JAnsi is not available.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 * @author Peter Royal
 * @author Ronald Jack Jenkins Jr.
 */
public final class BukkitLoggerAdapter implements Logger {

	// Plugin reference.
	private static transient Plugin BUKKIT_PLUGIN;
	private static transient String BUKKIT_PLUGIN_NAME;
	// Configuration parameters.
	private static final String CONFIG_FALLBACK_DEFAULT_LOG_LEVEL = "info";
	private static final Map<Level, ColorMarker> CONFIG_FALLBACK_LEVEL_COLORS = BukkitLoggerAdapter.fallbackLevelColors();
	private static final boolean CONFIG_FALLBACK_SHOW_HEADER = false;
	private static final boolean CONFIG_FALLBACK_SHOW_LOG_NAME = false;
	private static final boolean CONFIG_FALLBACK_SHOW_SHORT_LOG_NAME = true;
	private static final boolean CONFIG_FALLBACK_SHOW_THREAD_NAME = false;
	private static final String CONFIG_KEY_DEFAULT_LOG_LEVEL = "slf4j.defaultLogLevel";
	private static final String CONFIG_KEY_LEVEL_COLORS = "slf4j.colors";
	private static final String CONFIG_KEY_PREFIX_LOG = "slf4j.log.";
	private static final String CONFIG_KEY_SHOW_HEADER = "slf4j.showHeader";
	private static final String CONFIG_KEY_SHOW_LOG_NAME = "slf4j.showLogName";
	private static final String CONFIG_KEY_SHOW_SHORT_LOG_NAME = "slf4j.showShortLogName";
	private static final String CONFIG_KEY_SHOW_THREAD_NAME = "slf4j.showThreadName";
	private static Level CONFIG_VALUE_DEFAULT_LOG_LEVEL;
	private static Map<Level, ColorMarker> CONFIG_VALUE_LEVEL_COLORS;
	private static boolean CONFIG_VALUE_SHOW_HEADER;
	private static boolean CONFIG_VALUE_SHOW_LOG_NAME;
	private static boolean CONFIG_VALUE_SHOW_SHORT_LOG_NAME;
	private static boolean CONFIG_VALUE_SHOW_THREAD_NAME;
	// Initialization lock.
	private static final Object INITIALIZATION_LOCK = new Object();
	// The logger name.
	private final String name;
	// The short name of this simple log instance
	private final ColorMapper mapper = ColorMapperFactory.create();
	private transient String shortLogName = null;

	// NOTE: BukkitPluginLoggerAdapter constructor should have only package access
	// so that only BukkitPluginLoggerFactory be able to create one.
	BukkitLoggerAdapter(final String name) {
		this.name = name;
	}

	/**
	 * (Re)initializes all SLF4Bukkit loggers in this plugin, relying on the YAML
	 * configuration of the plugin.
	 *
	 * @param reinitialize
	 *          set to {@code true} to reinitialize all loggers, e.g. after
	 *          reloading the plugin config.
	 */
	public static void init(final boolean reinitialize) {
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			// Do not re-initialize unless requested.
			if (reinitialize) {
				BukkitLoggerAdapter.BUKKIT_PLUGIN = null;
				BukkitLoggerAdapter.BUKKIT_PLUGIN_NAME = null;
			} else if (BukkitLoggerAdapter.BUKKIT_PLUGIN != null) {return;}
			// Get a reference to the plugin in this classloader.
			if (BukkitLoggerAdapter.BUKKIT_PLUGIN_NAME == null) {
				InputStream pluginYmlFile = null;
				try {
					pluginYmlFile = BukkitLoggerAdapter.class.getClassLoader()
								.getResource("plugin.yml")
								.openStream();
					final Yaml yaml = new Yaml();
					@SuppressWarnings("rawtypes")
					final Map pluginYml = (Map) yaml.load(pluginYmlFile);
					BukkitLoggerAdapter.BUKKIT_PLUGIN_NAME = (String) pluginYml.get("name");
				}
				catch (final IOException e) {
					throw new IllegalStateException(e);
				}
				finally {
					if (pluginYmlFile != null) {
						try {
							pluginYmlFile.close();
						}
						catch (final IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			// Try to get the plugin. The logging system will be considered
			// uninitialized until this becomes non-null. While it is null, the Bukkit
			// server logger will be used instead of the plugin logger, and all
			// default configuration options will be used.
			BukkitLoggerAdapter.BUKKIT_PLUGIN = Bukkit.getPluginManager()
						.getPlugin(BukkitLoggerAdapter.BUKKIT_PLUGIN_NAME);
			// Get the configuration values.
			// 1. Look in the plugin's on-disk config.
			// 2. If the value is absent, use the plugin's built-in config.
			// 3. If the value is absent, use the default values hardcoded above.
			// (1 and 2 are handled by using the Bukkit API.)
			BukkitLoggerAdapter.CONFIG_VALUE_DEFAULT_LOG_LEVEL = BukkitLoggerAdapter.stringToLevel(BukkitLoggerAdapter.getStringProperty(BukkitLoggerAdapter.CONFIG_KEY_DEFAULT_LOG_LEVEL,
						BukkitLoggerAdapter.CONFIG_FALLBACK_DEFAULT_LOG_LEVEL));
			if (BukkitLoggerAdapter.CONFIG_VALUE_DEFAULT_LOG_LEVEL == null) {
				BukkitLoggerAdapter.CONFIG_VALUE_DEFAULT_LOG_LEVEL = BukkitLoggerAdapter.stringToLevel(BukkitLoggerAdapter.CONFIG_FALLBACK_DEFAULT_LOG_LEVEL);
			}
			BukkitLoggerAdapter.CONFIG_VALUE_LEVEL_COLORS = BukkitLoggerAdapter.getLevelColorsMap(BukkitLoggerAdapter.CONFIG_KEY_LEVEL_COLORS,
						BukkitLoggerAdapter.CONFIG_FALLBACK_LEVEL_COLORS);
			BukkitLoggerAdapter.CONFIG_VALUE_SHOW_HEADER = BukkitLoggerAdapter.getBooleanProperty(BukkitLoggerAdapter.CONFIG_KEY_SHOW_HEADER,
						BukkitLoggerAdapter.CONFIG_FALLBACK_SHOW_HEADER);
			BukkitLoggerAdapter.CONFIG_VALUE_SHOW_LOG_NAME = BukkitLoggerAdapter.getBooleanProperty(BukkitLoggerAdapter.CONFIG_KEY_SHOW_LOG_NAME,
						BukkitLoggerAdapter.CONFIG_FALLBACK_SHOW_LOG_NAME);
			BukkitLoggerAdapter.CONFIG_VALUE_SHOW_SHORT_LOG_NAME = false;
			BukkitLoggerAdapter.CONFIG_VALUE_SHOW_THREAD_NAME = false;
		}
	}

	/**
	 * Returns the fallback map of logging levels to their default colors.
	 *
	 * @return never null.
	 */
	private static Map<Level, ColorMarker> fallbackLevelColors() {
		return ImmutableMap.<Level, ColorMarker>builder()
					.put(Level.ERROR, ColorMarker.RED)
					.put(Level.WARN, ColorMarker.YELLOW)
					.put(Level.INFO, ColorMarker.NONE)
					.put(Level.DEBUG, ColorMarker.NONE)
					.put(Level.TRACE, ColorMarker.NONE).build();
	}

	/**
	 * Returns a boolean property from the Bukkit plugin config.
	 *
	 * @param name
	 *          the desired property.
	 * @param defaultValue
	 *          the fallback value returned by this method.
	 * @return {@code defaultValue} if the Bukkit plugin is not available, if the
	 *         desired property is not defined in the config, or if the desired
	 *         property's value is not either "true" or "false"
	 *         (case-insensitive).
	 */
	private static boolean getBooleanProperty(final String name,
				final boolean defaultValue) {
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			if (BukkitLoggerAdapter.BUKKIT_PLUGIN == null) {return defaultValue;}
			final String prop = BukkitLoggerAdapter.BUKKIT_PLUGIN.getConfig()
						.getString(name);
			if ("true".equalsIgnoreCase(prop)) {return true;}
			if ("false".equalsIgnoreCase(prop)) {return false;}
			return defaultValue;
		}
	}

	/**
	 * Returns the most appropriate logger.
	 *
	 * @return the logger for the plugin if available; otherwise the server
	 *         logger. Never null.
	 */
	private static java.util.logging.Logger getBukkitLogger() {
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			return BukkitLoggerAdapter.BUKKIT_PLUGIN == null ? Bukkit.getLogger()
						: BukkitLoggerAdapter.BUKKIT_PLUGIN.getLogger();
		}
	}

	/**
	 * Returns the map of logging levels to colors, taken from the Bukkit plugin
	 * config. For each relevant entry in the plugin config, if either the key
	 * name or the value name is invalid, that entry is ignored and the default
	 * value is used instead.
	 *
	 * @param property
	 *          the config property where the map exists.
	 * @param defaultValues
	 *          the fallback values returned by this method.
	 * @return never null, always contains one mapping for each {@link Level}, and
	 *         contains no null keys/values. Equal to {@code defaultValue} if the
	 *         Bukkit plugin is not available, or if the desired property is not
	 *         defined in the config.
	 */
	private static Map<Level, ColorMarker>
	getLevelColorsMap(final String property,
				final Map<Level, ColorMarker> defaultValues) {
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			// Check for the plugin.
			if (BukkitLoggerAdapter.BUKKIT_PLUGIN == null) {return defaultValues;}
			final ConfigurationSection config = BukkitLoggerAdapter.BUKKIT_PLUGIN.getConfig()
						.getConfigurationSection(property);
			// Quit if the config isn't specified.
			if (config == null) {return defaultValues;}
			// Translate each portion of the config. Skip invalid keys/values.
			final Map<String, Object> configValues = config.getValues(false);
			final Map<Level, ColorMarker> convertedConfigValues = new HashMap<Level, ColorMarker>();
			for (final Map.Entry<String, Object> configValue : configValues.entrySet()) {
				final String levelName = configValue.getKey().toUpperCase();
				final String formatName = configValue.getValue().toString()
							.toUpperCase();
				Level level;
				ColorMarker format;
				try {
					level = Level.valueOf(levelName);
					format = ColorMarker.valueOf(formatName);
				}
				catch (final IllegalArgumentException e) {
					// This is expected, so don't log it.
					continue;
				}
				convertedConfigValues.put(level, format);
			}
			// Merge the default and config-based map; the latter takes priority.
			final Map<Level, ColorMarker> finalConfigValues = new HashMap<Level, ColorMarker>();
			finalConfigValues.putAll(defaultValues);
			finalConfigValues.putAll(convertedConfigValues);
			// Done; cast as immutable.
			return ImmutableMap.<Level, ColorMarker>builder()
						.putAll(finalConfigValues).build();
		}
	}

	/**
	 * Returns a string property from the Bukkit plugin config.
	 *
	 * @param name
	 *          the desired property.
	 * @param defaultValue
	 *          the fallback value returned by this method.
	 * @return {@code defaultValue} if the Bukkit plugin is not available, or if
	 *         the desired property is not defined in the config.
	 */
	private static String getStringProperty(final String name,
				final String defaultValue) {
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			if (BukkitLoggerAdapter.BUKKIT_PLUGIN == null) {return defaultValue;}
			final String prop = BukkitLoggerAdapter.BUKKIT_PLUGIN.getConfig()
						.getString(name);
			return (prop == null) ? defaultValue : prop;
		}
	}

	/**
	 * Converts an SLF4J logging level to a Bukkit logging level.
	 *
	 * <ul>
	 * <li>{@link Level#ERROR} maps to {@link java.util.logging.Level#SEVERE}.</li>
	 * <li>{@link Level#WARN} maps to {@link java.util.logging.Level#WARNING}.</li>
	 * <li>All others map to {@link java.util.logging.Level#INFO} (Bukkit won't
	 * log any messages higher than {@code INFO}).</li>
	 * </ul>
	 *
	 * @param slf4jLevel
	 *          any SLF4J logging level.
	 * @return never null.
	 */
	private static java.util.logging.Level
	slf4jLevelIntToBukkitJULLevel(final Level slf4jLevel) {
		java.util.logging.Level julLevel;
		switch (slf4jLevel) {
			case ERROR:
				julLevel = java.util.logging.Level.SEVERE;
				break;
			case WARN:
				julLevel = java.util.logging.Level.WARNING;
				break;
			default:
				// In Bukkit, Only the SEVERE, WARNING and INFO JUL levels are enabled,
				// so SLF4J's TRACE and DEBUG levels must be logged at Bukkit's INFO
				// level.
				julLevel = java.util.logging.Level.INFO;
				break;
		}
		return julLevel;
	}

	/**
	 * Convert YAML logging level properties to SLF4J level objects.
	 *
	 * @param levelStr
	 *          the level property value from the YAML config.
	 * @return null iff the input does not map to a SLF4J logging level name in a
	 *         case-insensitive fashion.
	 */
	private static Level stringToLevel(final String levelStr) {
		if ("trace".equalsIgnoreCase(levelStr)) {
			return Level.TRACE;
		} else if ("debug".equalsIgnoreCase(levelStr)) {
			return Level.DEBUG;
		} else if ("info".equalsIgnoreCase(levelStr)) {
			return Level.INFO;
		} else if ("warn".equalsIgnoreCase(levelStr)) {
			return Level.WARN;
		} else if ("error".equalsIgnoreCase(levelStr)) {
			return Level.ERROR;
		} else {
			return null;
		}
	}

	@Override
	public void debug(final Marker marker, final String msg) {
		if (!this.isDebugEnabled()) {return;}
		this.log(Level.DEBUG, marker, msg, null);
	}

	@Override
	public void debug(final Marker marker, final String format, final Object arg) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, marker, format, arg, null);
	}

	@Override
	public void debug(final Marker marker, final String format,
				final Object... arguments) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, marker, format, arguments);
	}

	@Override
	public void debug(final Marker marker, final String format,
				final Object arg1, final Object arg2) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, marker, format, arg1, arg2);
	}

	@Override
	public void debug(final Marker marker, final String msg, final Throwable t) {
		if (!this.isDebugEnabled()) {return;}
		this.log(Level.DEBUG, marker, msg, t);
	}

	@Override
	public void debug(final String msg) {
		if (!this.isDebugEnabled()) {return;}
		this.log(Level.DEBUG, null, msg, null);
	}

	@Override
	public void debug(final String format, final Object arg) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, null, format, arg, null);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, null, format, arguments);
	}

	@Override
	public void debug(final String format, final Object arg1, final Object arg2) {
		if (!this.isDebugEnabled()) {return;}
		this.formatAndLog(Level.DEBUG, null, format, arg1, arg2);
	}

	@Override
	public void debug(final String msg, final Throwable t) {
		if (!this.isDebugEnabled()) {return;}
		this.log(Level.DEBUG, null, msg, t);
	}

	@Override
	public void error(final Marker marker, final String msg) {
		if (!this.isErrorEnabled()) {return;}
		this.log(Level.ERROR, marker, msg, null);
	}

	@Override
	public void error(final Marker marker, final String format, final Object arg) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, marker, format, arg, null);
	}

	@Override
	public void error(final Marker marker, final String format,
				final Object... arguments) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, marker, format, arguments);
	}

	@Override
	public void error(final Marker marker, final String format,
				final Object arg1, final Object arg2) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, marker, format, arg1, arg2);
	}

	@Override
	public void error(final Marker marker, final String msg, final Throwable t) {
		if (!this.isErrorEnabled()) {return;}
		this.log(Level.ERROR, marker, msg, t);
	}

	@Override
	public void error(final String msg) {
		if (!this.isErrorEnabled()) {return;}
		this.log(Level.ERROR, null, msg, null);
	}

	@Override
	public void error(final String format, final Object arg) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, null, format, arg, null);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, null, format, arguments);
	}

	@Override
	public void error(final String format, final Object arg1, final Object arg2) {
		if (!this.isErrorEnabled()) {return;}
		this.formatAndLog(Level.ERROR, null, format, arg1, arg2);
	}

	@Override
	public void error(final String msg, final Throwable t) {
		if (!this.isErrorEnabled()) {return;}
		this.log(Level.ERROR, null, msg, t);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void info(final Marker marker, final String msg) {
		if (!this.isInfoEnabled()) {return;}
		this.log(Level.INFO, marker, msg, null);
	}

	@Override
	public void info(final Marker marker, final String format, final Object arg) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, marker, format, arg, null);
	}

	@Override
	public void info(final Marker marker, final String format,
				final Object... arguments) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, marker, format, arguments);
	}

	@Override
	public void info(final Marker marker, final String format, final Object arg1,
				final Object arg2) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, marker, format, arg1, arg2);
	}

	@Override
	public void info(final Marker marker, final String msg, final Throwable t) {
		if (!this.isInfoEnabled()) {return;}
		this.log(Level.INFO, marker, msg, t);
	}

	@Override
	public void info(final String msg) {
		if (!this.isInfoEnabled()) {return;}
		this.log(Level.INFO, null, msg, null);
	}

	@Override
	public void info(final String format, final Object arg) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, null, format, arg, null);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, null, format, arguments);
	}

	@Override
	public void info(final String format, final Object arg1, final Object arg2) {
		if (!this.isInfoEnabled()) {return;}
		this.formatAndLog(Level.INFO, null, format, arg1, arg2);
	}

	@Override
	public void info(final String msg, final Throwable t) {
		if (!this.isInfoEnabled()) {return;}
		this.log(Level.INFO, null, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.isLevelEnabled(Level.DEBUG);
	}

	@Override
	public boolean isDebugEnabled(final Marker marker) {
		return this.isLevelEnabled(Level.DEBUG);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.isLevelEnabled(Level.ERROR);
	}

	@Override
	public boolean isErrorEnabled(final Marker marker) {
		return this.isLevelEnabled(Level.ERROR);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.isLevelEnabled(Level.INFO);
	}

	@Override
	public boolean isInfoEnabled(final Marker marker) {
		return this.isLevelEnabled(Level.INFO);
	}

	@Override
	public boolean isTraceEnabled() {
		return this.isLevelEnabled(Level.TRACE);
	}

	@Override
	public boolean isTraceEnabled(final Marker marker) {
		return this.isLevelEnabled(Level.TRACE);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.isLevelEnabled(Level.WARN);
	}

	@Override
	public boolean isWarnEnabled(final Marker marker) {
		return this.isLevelEnabled(Level.WARN);
	}

	@Override
	public void trace(final Marker marker, final String msg) {
		if (!this.isTraceEnabled()) {return;}
		this.log(Level.TRACE, marker, msg, null);
	}

	@Override
	public void trace(final Marker marker, final String format, final Object arg) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, marker, format, arg, null);
	}

	@Override
	public void trace(final Marker marker, final String format,
				final Object... arguments) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, marker, format, arguments);
	}

	@Override
	public void trace(final Marker marker, final String format,
				final Object arg1, final Object arg2) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, marker, format, arg1, arg2);
	}

	@Override
	public void trace(final Marker marker, final String msg, final Throwable t) {
		if (!this.isTraceEnabled()) {return;}
		this.log(Level.TRACE, marker, msg, t);
	}

	@Override
	public void trace(final String msg) {
		if (!this.isTraceEnabled()) {return;}
		this.log(Level.TRACE, null, msg, null);
	}

	@Override
	public void trace(final String format, final Object arg) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, null, format, arg, null);
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, null, format, arguments);
	}

	@Override
	public void trace(final String format, final Object arg1, final Object arg2) {
		if (!this.isTraceEnabled()) {return;}
		this.formatAndLog(Level.TRACE, null, format, arg1, arg2);
	}

	@Override
	public void trace(final String msg, final Throwable t) {
		if (!this.isTraceEnabled()) {return;}
		this.log(Level.TRACE, null, msg, t);
	}

	@Override
	public void warn(final Marker marker, final String msg) {
		if (!this.isWarnEnabled()) {return;}
		this.log(Level.WARN, marker, msg, null);
	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, marker, format, arg, null);
	}

	@Override
	public void warn(final Marker marker, final String format,
				final Object... arguments) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, marker, format, arguments);
	}

	@Override
	public void warn(final Marker marker, final String format, final Object arg1,
				final Object arg2) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, marker, format, arg1, arg2);
	}

	@Override
	public void warn(final Marker marker, final String msg, final Throwable t) {
		if (!this.isWarnEnabled()) {return;}
		this.log(Level.WARN, marker, msg, t);
	}

	@Override
	public void warn(final String msg) {
		if (!this.isWarnEnabled()) {return;}
		this.log(Level.WARN, null, msg, null);
	}

	@Override
	public void warn(final String format, final Object arg) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, null, format, arg, null);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, null, format, arguments);
	}

	@Override
	public void warn(final String format, final Object arg1, final Object arg2) {
		if (!this.isWarnEnabled()) {return;}
		this.formatAndLog(Level.WARN, null, format, arg1, arg2);
	}

	@Override
	public void warn(final String msg, final Throwable t) {
		if (!this.isWarnEnabled()) {return;}
		this.log(Level.WARN, null, msg, t);
	}

	/**
	 * Computes this logger's short name, which is equivalent to the short Java
	 * package name format (e.g. a logger named "info.ronjenkins.bukkit.MyPlugin"
	 * would have a short name of "i.r.b.MyPlugin").
	 *
	 * @return never null.
	 */
	private String computeShortName() {
		final List<String> splitName = new ArrayList<String>();
		splitName.addAll(Arrays.asList(this.name.split("\\.")));
		final int shortNameLength = ((splitName.size() - 1) * 2)
					+ splitName.get(splitName.size() - 1).length();
		final String finalName = splitName.remove(splitName.size() - 1);
		final StringBuffer shortName = new StringBuffer(shortNameLength);
		for (final String part : splitName) {
			shortName.append(part.charAt(0)).append('.');
		}
		shortName.append(finalName);
		return shortName.toString();
	}

	/**
	 * Computes this logger's current logging level, based on the Bukkit plugin
	 * config.
	 *
	 * @return the value of "slf4j.defaultLogLevel" if neither this logger nor any
	 *         of its ancestors define a logging level.
	 */
	private Level determineCurrentLevel() {
		// Compute the current level, which may be null.
		String tempName = this.name;
		Level level = null;
		int indexOfLastDot = tempName.length();
		while ((level == null) && (indexOfLastDot > -1)) {
			tempName = tempName.substring(0, indexOfLastDot);
			level = BukkitLoggerAdapter.stringToLevel(BukkitLoggerAdapter.getStringProperty(BukkitLoggerAdapter.CONFIG_KEY_PREFIX_LOG
									+ tempName,
						null));
			indexOfLastDot = String.valueOf(tempName).lastIndexOf(".");
		}
		// Return the default value if we got null.
		return (level == null) ? BukkitLoggerAdapter.CONFIG_VALUE_DEFAULT_LOG_LEVEL
					: level;
	}

	/**
	 * For formatted messages, first substitute arguments and then log.
	 *
	 * @param level
	 *          the level of this message.
	 * @param marker
	 *          the marker to use for this message, may be null.
	 * @param format
	 *          the message format string.
	 * @param arguments
	 *          3 or more arguments.
	 */
	private void formatAndLog(final Level level, final Marker marker,
				final String format, final Object... arguments) {
		if (!this.isLevelEnabled(level)) {return;}
		final FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
		this.log(level, marker, tp.getMessage(), tp.getThrowable());
	}

	/**
	 * For formatted messages, first substitute arguments and then log.
	 *
	 * @param level
	 *          the level of this message.
	 * @param marker
	 *          the marker to use for this message, may be null.
	 * @param format
	 *          the message format string.
	 * @param arg1
	 *          format argument #1.
	 * @param arg2
	 *          format argument #2.
	 */
	private void formatAndLog(final Level level, final Marker marker,
				final String format, final Object arg1,
				final Object arg2) {
		if (!this.isLevelEnabled(level)) {return;}
		final FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
		this.log(level, marker, tp.getMessage(), tp.getThrowable());
	}

	/**
	 * Is the given log level currently enabled?
	 *
	 * @param logLevel
	 *          is this level enabled?
	 * @return true if enabled, false if disabled.
	 */
	private boolean isLevelEnabled(final Level logLevel) {
		// Ensure that SLF4Bukkit is initialized. Every public API call passes
		// through this method, so this is the appropriate place to ensure
		// initialization.
		BukkitLoggerAdapter.init(false);
		// log level are numerically ordered so can use simple numeric comparison
		//
		// the PLUGIN.getLogger().isLoggable() check avoids the unconditional
		// construction of location data for disabled log statements. As of
		// 2008-07-31, callers of this method do not perform this check. See also
		// http://jira.qos.ch/browse/SLF4J-81
		final Level currentLogLevel = this.determineCurrentLevel();
		return (logLevel.toInt() >= currentLogLevel.toInt())
					&& (BukkitLoggerAdapter.getBukkitLogger().isLoggable(BukkitLoggerAdapter.slf4jLevelIntToBukkitJULLevel(logLevel)));
	}

	/**
	 * Assembles the final log message and sends it to the appropriate Bukkit
	 * logger.
	 *
	 * @param level
	 *          the desired log level of the message.
	 * @param marker
	 *          the marker to use for this message, may be null.
	 * @param message
	 *          the message to be logged.
	 * @param throwable
	 *          the exception to be logged, may be null.
	 */
	private void log(final Level level, final Marker marker,
				final String message, final Throwable throwable) {
		final java.util.logging.Logger logger;
		synchronized (BukkitLoggerAdapter.INITIALIZATION_LOCK) {
			// Ensure that the logger will accept this request.
			if (!this.isLevelEnabled(level)) {return;}
			// Determine which logger will be used.
			logger = BukkitLoggerAdapter.getBukkitLogger();
		}

		// Start building the log message.
		final StringBuilder buf = new StringBuilder(32);
		boolean hasHeader = false;

		// Use the marker, if applicable. Otherwise, use the default color for
		// this level.
		if (marker instanceof ColorMarker) {
			buf.append(((ColorMarker) marker).getValue());
		} else {
			buf.append(BukkitLoggerAdapter.CONFIG_VALUE_LEVEL_COLORS.get(level)
						.getValue());
		}

		// Indicate that this message comes from SLF4J, if desired.
		if (BukkitLoggerAdapter.CONFIG_VALUE_SHOW_HEADER) {
			hasHeader = true;
			buf.append("[SLF4J]");
		}

		// Print a readable representation of the log level, but only for log levels
		// that Bukkit would otherwise eat.
		switch (level) {
			case TRACE:
				hasHeader = true;
				buf.append("[TRACE]");
				break;
			case DEBUG:
				hasHeader = true;
				buf.append("[DEBUG]");
				break;
			default:
				break;
		}

		// Append the current thread name, if desired.
		if (BukkitLoggerAdapter.CONFIG_VALUE_SHOW_THREAD_NAME) {
			hasHeader = true;
			buf.append('[');
			buf.append(Thread.currentThread().getName());
			buf.append("]");
		}

		// Buffer the current output with a space, unless there is no output.
		if (hasHeader) {
			buf.append(' ');
		}

		// Append the name of the log instance, if desired.
		if (BukkitLoggerAdapter.CONFIG_VALUE_SHOW_LOG_NAME) {
			buf.append('{').append(this.name).append("} ");
		} else if (BukkitLoggerAdapter.CONFIG_VALUE_SHOW_SHORT_LOG_NAME) {
			if (this.shortLogName == null) {
				this.shortLogName = this.computeShortName();
			}
			buf.append('{').append(this.shortLogName).append("} ");
		}

		// Append the message.
		buf.append(message);

		// Append the throwable, if applicable.
		if (throwable != null) {
			buf.append('\n').append(ExceptionUtils.getFullStackTrace(throwable)
						.trim());
		}

		// Append a reset directive.
		buf.append(ChatColor.RESET);

		// Log the message.
		logger.log(BukkitLoggerAdapter.slf4jLevelIntToBukkitJULLevel(level),
					mapper.map(buf.toString()));
	}
}
