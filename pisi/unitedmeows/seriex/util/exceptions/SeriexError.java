package pisi.unitedmeows.seriex.util.exceptions;

public class SeriexError extends Error {
	public SeriexError(String string) {
		super(string);
	}

	public SeriexError(String string, Throwable throwable) {
		super(string, throwable);
	}

	private static final long serialVersionUID = 6743149002556725943L;
}
