package pisi.unitedmeows.seriex.managers.future;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;

// note: you cant delete futures until they are done get real
public class FutureManager extends Manager {
	private List<Future<?>> futures = new GlueList<>();

	private AtomicBoolean stopLoopingThread = new AtomicBoolean(false);

	@Override
	public void start(Seriex seriex) {
		futures.clear();
	}

	public void addFuture(Future<?> future) {
		futures.add(future);
	}

	public void updateFutures() {
		futures.removeIf(Future::hasSet);
	}

	private boolean done = true;

	@Override
	public void cleanup() throws SeriexException {
		done = checkFutures(done);
		Thread mainThread = Seriex.get().primaryThread();
		Async.async_loop_condition(() -> {
			if (done) {
				Seriex.logger().info("All futures finished.");
				synchronized (mainThread) {
					mainThread.notifyAll();
				}
				stopLoopingThread.set(true);
			}
			// maybe?
			done = checkFutures(done);
		}, 50L, () -> !stopLoopingThread.get());
		while (!done) {
			Seriex.logger().info("Waiting for futures to be finished.");
			// runs once
			synchronized (mainThread) {
				try {
					mainThread.wait(Duration.ofSeconds(30).toMillis());
				}
				catch (InterruptedException e) {
					e.printStackTrace();
					mainThread.interrupt();
				}
			}
			// set again or infinite loop
			// this should not work because we literally stopped the main thread
			// so we check again using another thread above
			done = checkFutures(done);
		}
	}

	private boolean checkFutures(boolean done) {
		for (int i = 0; i < futures.size(); i++) {
			done &= futures.get(i).hasSet();
		}
		return done;
	}
}
