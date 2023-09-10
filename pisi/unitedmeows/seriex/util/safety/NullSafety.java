package pisi.unitedmeows.seriex.util.safety;

import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class NullSafety {
	public static void assertNull(Object object) {
		if (object != null)
			throw SeriexException.create(object.toString() + " isn't null when it should be!");
	}

	public static void assertNotNull(Object object) {
		if (object == null)
			throw SeriexException.create("Something is null when it should not be!");
	}
}
