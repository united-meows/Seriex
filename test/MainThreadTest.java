package test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;

public class MainThreadTest {
	public static void main(String... args) throws InterruptedException {
		Thread mainThread = Thread.currentThread();
		// 7
		// 0 		0 	 0   0
		// 2^3 2^2 2^1 2^0
		// 0b0111
		// 0 0 0 0 0
		// 4 3 2 1 0
		// 16 + 8 + 7
		// i^2
		/*
		 * x
		 * 
		 * x / 16 = y1
		 * x % 16 = z1
		 * 
		 * y1 / 16 = y2
		 * 
		 */
		//	2173
		// 16 x 16 x 8 = 2048
		//	16 x 7 = 112
		// C = 13
		//
		//	4688
		//
		// 16 x 16 x 16 = 4096
		// 16 x 16 x 2 = 512
		//	16 x 5 = 08
		//
		// 9059
		// 16 x 16 x 16 x 2 = 8192
		// 16 x 16 x ? = 768
		//
		// 22460
		// 16 x 16 x 16 x 5 = 20480
		// 16 x 16 x 7 = 1792
		// 16 x 11 = 176
		// C
		// 121913
		// 16 x 16 x 16 x 16 x 1
		// 56,377 left
		// 16 x 16 x 16 x 13 = 56,377
		// 3129  - 3,072
		// 16 x 16 x 12 = 2304
		// 16 x 3
		// 9
		Seriex.logger().info(0xA001 + " " + 0x1DC39 + " " + Math.pow(2, 16) / 8);
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
