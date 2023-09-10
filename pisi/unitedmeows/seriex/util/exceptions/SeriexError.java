package pisi.unitedmeows.seriex.util.exceptions;

import java.io.Serial;

public class SeriexError extends Error {
	public SeriexError(String string) {
		super(string);
	}

	public SeriexError(String string, Throwable throwable) {
		super(string, throwable);
	}

	@Serial
	private static final long serialVersionUID = 6743149002556725943L;
}
