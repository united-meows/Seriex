package pisi.unitedmeows.seriex.util.exceptions;

public class SeriexException extends RuntimeException {

	private static final long serialVersionUID = 8799296359192399031L;

	private SeriexException(String string) {
		super(string);
	}

	public static SeriexException create(String message) {
		return new SeriexException(message);
	}
}
