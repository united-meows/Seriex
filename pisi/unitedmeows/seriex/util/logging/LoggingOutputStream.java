package pisi.unitedmeows.seriex.util.logging;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LoggingOutputStream extends ByteArrayOutputStream {

	private String lineSeparator;

	private final Logger logger;
	private final boolean err;

	public LoggingOutputStream(Logger logger, boolean err) {
		super();
		this.logger = logger;
		lineSeparator = System.getProperty("line.separator");
		this.err = err;
	}

	public void flush() throws IOException {

		String log;
		synchronized (this) {
			super.flush();
			log = this.toString().trim();
			super.reset();

			if (log.length() == 0 || log.equals(lineSeparator)) {
				// avoid empty records
				return;
			}

			if(err) {
				logger.error(log);
				return;
			}

			logger.info(log);
		}
	}
}
