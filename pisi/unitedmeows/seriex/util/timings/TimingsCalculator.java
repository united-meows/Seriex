package pisi.unitedmeows.seriex.util.timings;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.IExec;

public enum TimingsCalculator {
	GET;

	public void benchmark(final IExec exec, final String name) {
		final long ms = System.currentTimeMillis();
		exec.run();
		Seriex.logger().debug(String.format("Loaded %s in %d ms!", name, Long.valueOf(System.currentTimeMillis() - ms)));
	}
}
