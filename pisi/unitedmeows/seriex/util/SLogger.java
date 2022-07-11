package pisi.unitedmeows.seriex.util;

import static java.util.Locale.*;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.logging.Level;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.yystal.logger.ILogger;

// yfile is broken loololo using normal file ;(
public class SLogger implements ILogger {
	private Time time = Time.DAY_MONTH_YEAR_FULL;
	private final Class<?> clazz;
	private String name;
	private boolean prefix;
	private boolean save;
	private int bufferSize;
	private File file;
	private String[] buffer;
	private boolean colored = false;
	private int bufferIndex;
	private static final Ansi.Color FATAL_COLOR = RED;

	public SLogger(final Class<?> _clazz) {
		clazz = _clazz;
		name = "[" + clazz.getSimpleName().toUpperCase(ROOT) + "]";
	}

	public SLogger(final Class<?> _clazz, final String _name) {
		clazz = _clazz;
		name = "[" + _name + "]";
	}

	private void post(String text) {
		if (save) {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("[").append(generateTime()).append("] ");
			if (prefix) {
				stringBuilder.append(name).append(" ");
			}
			stringBuilder.append(text);
			buffer[bufferIndex++] = stringBuilder.toString();
			if (bufferIndex >= bufferSize) {
				flush();
			}
		}
	}

	private void flush() {
		try (FileWriter writer = new FileWriter(this.file, true)) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < buffer.length; i++) {
				stringBuilder.append(buffer[i]);
				stringBuilder.append(System.lineSeparator());
			}
			writer.write(stringBuilder.toString());
			bufferIndex = 0;
		}
		catch (Exception e) {
			e.printStackTrace();
			Seriex.logger().fatal("Couldnt save to log file!");
		}
	}

	public String generateAnsiString(String text, String prefix_, Color textColor, boolean special, boolean spigot) {
		StringBuilder builder = new StringBuilder();
		StringBuilder lolSlowCode = new StringBuilder();
		String value = String.format("[%s] ", prefix_);
		String time = generateTime();
		lolSlowCode.append("[").append(time).append("] ");
		if (prefix) {
			lolSlowCode.append(name).append(" ");
		}
		lolSlowCode.append(value);
		lolSlowCode.append(text);
		if (!spigot) {
			builder.append(ansi().fg(WHITE).bold().a("[").a(time).a("] "));
		}
		if (prefix) {
			builder.append(name);
			builder.append(" ");
		}
		if (special) {
			if (!spigot) {
				builder.append(ansi().bold().fg(MAGENTA).a(value).boldOff());
			}
			builder.append(ansi().bg(FATAL_COLOR).fg(textColor).a(text).reset());
		} else {
			if (!spigot) {
				builder.append(ansi().bold().fg(MAGENTA).a(value).boldOff());
			}
			builder.append(ansi().fg(textColor).a(text).reset());
		}
		String string = lolSlowCode.toString();
		post(string);
		return colored ? builder.toString() : string;
	}

	@Override
	public void info(String text) {
		if (Seriex.available()) {
			Seriex.get().getServer().getLogger().info(generateAnsiString(text, "INFO", CYAN, false, true));
		} else {
			System.out.println(generateAnsiString(text, "INFO", CYAN, false, false));
		}
	}

	@Override
	public void warn(String text) {
		if (Seriex.available()) {
			Seriex.get().getServer().getLogger().warning(generateAnsiString(text, "WARN", RED, false, true));
		} else {
			System.out.println(generateAnsiString(text, "WARN", RED, false, false));
		}
	}

	@Override
	public void fatal(String text) {
		if (Seriex.available()) {
			Seriex.get().getServer().getLogger().log(Level.SEVERE, generateAnsiString(text, "FATAL", BLACK, true, true));
		} else {
			System.out.println(generateAnsiString(text, "FATAL", BLACK, true, false));
		}
	}

	@Override
	public void debug(String text) {
		if (Seriex.available()) {
			Seriex.get().getServer().getLogger().info(generateAnsiString(text, "DEBUG", YELLOW, false, true));
		} else {
			System.out.println(generateAnsiString(text, "DEBUG", YELLOW, false, false));
		}
	}

	@Override
	public void log(Enum<?> type, String text) {
		/* do nothing */
	}

	@Override
	public void print(String text) {
		/* do nothing */
	}

	public Time time() {
		return time;
	}

	public SLogger time(Time _time) {
		time = _time;
		return this;
	}

	private String generateTime() {
		switch (time) {
			case MILLISECONDS:
				return String.valueOf(System.currentTimeMillis());
			case HOUR_MINUTES: {
				LocalDateTime localDateTime = LocalDateTime.now();
				return localDateTime.getHour() + ":" + localDateTime.getMinute();
			}
			case HOUR_MINUTES_SECONDS: {
				LocalDateTime localDateTime = LocalDateTime.now();
				return localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond();
			}
			case HOUR: {
				return String.valueOf(LocalDateTime.now().getHour());
			}
			case YEAR_MONTH_DAY_FULL: {
				LocalDateTime localDateTime = LocalDateTime.now();
				return localDateTime.getYear() + "-" + localDateTime.getMonthValue() + "-" + localDateTime.getDayOfMonth() + " " + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":"
							+ localDateTime.getSecond();
			}
			case DAY_MONTH_YEAR_FULL: {
				LocalDateTime localDateTime = LocalDateTime.now();
				return localDateTime.getDayOfMonth() + "-" + localDateTime.getMonthValue() + "-" + localDateTime.getYear() + " " + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":"
							+ localDateTime.getSecond();
			}
			default:
				return "";
		}
	}

	public SLogger colored(boolean state) {
		this.colored = state;
		return this;
	}

	public SLogger outputToFile(File file) {
		return outputToFile(file, 1, false);
	}

	public SLogger outputToFile(File file, int bufferSize, boolean delete) {
		this.file = file;
		if (file.exists() && delete) {
			file.delete();
		}
		this.save = true;
		this.bufferSize = bufferSize;
		if (buffer == null) {
			buffer = new String[bufferSize];
		}
		return this;
	}

	public SLogger stopOutput() {
		flush();
		save = false;
		return this;
	}

	public SLogger bufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		if (buffer != null) {
			flush();
		}
		buffer = new String[bufferSize];
		return this;
	}

	public SLogger prefix(String value) {
		this.prefix = true;
		name = "[" + value + "]";
		return this;
	}

	public enum Time {
		MILLISECONDS,
		HOUR_MINUTES,
		HOUR_MINUTES_SECONDS,
		HOUR,
		DAY_MONTH_YEAR_FULL,
		YEAR_MONTH_DAY_FULL,
		NO_TIME
	}

	@Override
	public void warn(String text, Object... args) {
		warn(String.format(text, args));
	}

	@Override
	public void info(String text, Object... args) {
		info(String.format(text, args));
	}

	@Override
	public void print(String text, Object... args) {
		print(String.format(text, args));
	}

	@Override
	public void fatal(String text, Object... args) {
		fatal(String.format(text, args));
	}

	@Override
	public void debug(String text, Object... args) {
		debug(String.format(text, args));
	}
}
