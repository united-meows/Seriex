package pisi.unitedmeows.seriex.util.timings;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.yystal.YYStal;

public class Benchmark {
	public static void profile(Runnable runnable, String name) {
		YYStal.startWatcher();
		runnable.run();
		Seriex.get().logger().debug("{} took {} ms!", name, YYStal.stopWatcher());
	}

	public static void profilec(Runnable runnable, String string) {
		YYStal.startWatcher();
		runnable.run();
		Seriex.get().logger().debug(String.format(string, YYStal.stopWatcher()));
	}
}
