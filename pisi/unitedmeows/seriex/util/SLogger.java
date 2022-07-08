package pisi.unitedmeows.seriex.util;

import static java.lang.System.*;
import static java.util.Locale.*;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

import java.io.File;
import java.time.LocalDateTime;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;

import pisi.unitedmeows.yystal.file.YFile;
import pisi.unitedmeows.yystal.logger.ILogger;

public class SLogger implements ILogger {
	private Time time = Time.DAY_MONTH_YEAR_FULL;
	private final Class<?> clazz;
	private String name;
	private boolean prefix;
	private boolean save;
	private int bufferSize;
	private YFile file;
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
		file.write(buffer);
		bufferIndex = 0;
	}

	private void internalPrint(String text, String prefix_, Color textColor, boolean special) {
		String value = String.format("[%s] ", prefix_);
		if (colored) {
			out.print(ansi().eraseScreen().fg(CYAN).bold().a("[").a(generateTime()).a("] "));
			if (prefix) {
				out.print(name);
				out.print(" ");
			}
			if (special) {
				out.print(ansi().bg(FATAL_COLOR).fg(textColor).a(value));
				out.println(ansi().bg(FATAL_COLOR).fg(textColor).a(text).reset());
			} else {
				out.print(ansi().fg(textColor).a(value));
				out.println(ansi().fg(textColor).a(text).reset());
			}
		} else {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("[").append(generateTime()).append("] ");
			if (prefix) {
				stringBuilder.append(name).append(" ");
			}
			stringBuilder.append(value);
			stringBuilder.append(text);
			out.println(stringBuilder.toString());
		}
		post(value + text);
	}

	@Override
	public void info(String text) {
		internalPrint(text, "INFO", GREEN, false);
	}

	@Override
	public void warn(String text) {
		internalPrint(text, "WARN", YELLOW, false);
	}

	@Override
	public void print(String text) {}

	@Override
	public void fatal(String text) {
		internalPrint(text, "FATAL", BLACK, true);
	}

	@Override
	public void debug(String text) {
		internalPrint(text, "DEBUG", MAGENTA, false);
	}

	@Override
	public void log(Enum<?> type, String text) {
		/* do nothing */
		// que pro
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
		if (state) {
			System.setProperty("jansi.passthrough", "true");
			System.setProperty("org.jline.terminal.dumb", "true");
			AnsiConsole.systemInstall();
		}
		return this;
	}

	public SLogger outputToFile(File file, int buffer) {
		return outputToFile(new YFile(file), buffer);
	}

	public SLogger outputToFile(File file) {
		return outputToFile(new YFile(file), 1);
	}

	public SLogger outputToFile(YFile file) {
		return outputToFile(file, 1);
	}

	public SLogger outputToFile(YFile file, int bufferSize) {
		this.file = file;
		if (file.file().exists()) {
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
