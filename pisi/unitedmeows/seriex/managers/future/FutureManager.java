package pisi.unitedmeows.seriex.managers.future;

import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;

// note: you cant delete futures until they are done get real
public class FutureManager extends Manager {
	private List<Future<?>> futures = new GlueList<>();

	@Override
	public void start(Seriex seriex) {
		futures.clear();
	}

	public void addFuture(Future<?> future) {
		futures.add(future);
	}

	public void updateFutures() {
		for (int i = 0; i < futures.size(); i++) {
			if (futures.get(i).hasSet()) {
				futures.remove(i);
				i--; // so we dont skip the next one
			}
		}
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
					mainThread.notify();
				}
			}
			// maybe?
			done = checkFutures(done);
		}, 50L, () -> true);
		while (!done) {
			Seriex.logger().info("Waiting for futures to be finished.");
			// runs once
			synchronized (mainThread) {
				try {
					mainThread.wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
					mainThread.interrupt();
				}
			}
			// set again or infinite loop
			done = checkFutures(done);
		}
	}

	private boolean checkFutures(boolean done) {
		for (int i = 0; i < futures.size(); i++) {
			Future<?> future = futures.get(i);
			done &= future.hasSet();
		}
		return done;
	}
}
