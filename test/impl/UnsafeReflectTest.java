package test.impl;

import static pisi.unitedmeows.seriex.util.unsafe.UnsafeReflect.getFieldValue;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.web.ConnectionUtils;

public class UnsafeReflectTest {
	public static void main(String... args) throws NoSuchFieldException {
		int times = 10_000_000;
		long startMS = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			getFieldValue(ConnectionUtils.class, "firstTime");
		}
		Seriex.logger().debug(String.valueOf(System.currentTimeMillis() - startMS));
	}
}
