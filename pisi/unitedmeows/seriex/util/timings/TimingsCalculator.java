package pisi.unitedmeows.seriex.util.timings;

import java.util.function.Consumer;

import pisi.unitedmeows.seriex.Seriex;

public enum TimingsCalculator {
	GET;

	public void benchmark(final Consumer<TimingsCalculator> func, final String name) {
		final long ms = System.currentTimeMillis();
		func.accept(this);
		Seriex.logger().debug(String.format("Loaded %s in %d ms!", name, (System.currentTimeMillis() - ms)));
	}
}
