package test;

import static pisi.unitedmeows.seriex.util.unsafe.UnsafeReflect.getFieldValue;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.cache.BasicCache;

public class UnsafeReflectTest {
	@SuppressWarnings("all")
	public static void main(String... args) throws NoSuchFieldException,SecurityException,IllegalArgumentException,IllegalAccessException {
		int times = 10_000_000;
		long totalUnsafeMS = 0L;
		BasicCache<Boolean> unsafe = new BasicCache<>((boolean) getFieldValue(Seriex.class, "loadedCorrectly"));
		for (int i = 0; i < times; i++) {
			boolean value = (boolean) getFieldValue(Seriex.class, "loadedCorrectly");
			unsafe.setLocked(unsafe.get() == value);
			long unsafeMS = System.currentTimeMillis();
			if (value != unsafe.get()) {
				unsafe.set(value);
			}
			totalUnsafeMS += System.currentTimeMillis() - unsafeMS;
		}
		Seriex.logger().debug(totalUnsafeMS + "");
	}
}
