package test.impl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;

public class MainThreadTest {
	public static void main(String... args) throws InterruptedException {
		Thread mainThread = Thread.currentThread();
		Seriex.logger().info(0x31 + "");
		AtomicInteger kek = new AtomicInteger();
		Future<Boolean> future = Async.async(() -> {
			Seriex.logger().debug("Waiting for operation to end...");
			for (int i = 0; i < 30000000; i++) {
				Pattern.compile("(((X)*Y)*Z)*");
			}
			Seriex.logger().fatal("Done (waited for %s ms)!", kek.get());
			return Boolean.TRUE;
		});
		Async.async_loop_condition(() -> {
			if (future.hasSet()) {
				synchronized (mainThread) {
					mainThread.notify();
				}
			}
		}, 5L, () -> true);
		while (!future.hasSet()) {
			kek.incrementAndGet(); // runs once
			synchronized (mainThread) {
				mainThread.wait();
			}
		}
		Seriex.logger().fatal("Trying to exit...");
		System.exit(1);
	}
}
