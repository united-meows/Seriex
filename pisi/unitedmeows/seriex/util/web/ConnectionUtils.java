package pisi.unitedmeows.seriex.util.web;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import pisi.unitedmeows.yystal.parallel.Async;

public class ConnectionUtils {
	private static boolean firstTime = true;
	private static AtomicBoolean atomicBoolean = new AtomicBoolean(false);
	private static String[] hosts = new String[] {
		"google.com", "amazon.com", "facebook.com", "apple.com",
	};

	public static boolean hasInternetConnection() {
		if (firstTime) {
			Async.async_loop(() -> {
				boolean connection = connectedToAnyHost();
				atomicBoolean.set(connection);
			}, Duration.ofMinutes(2).toMillis());
			firstTime = false;
		}
		return atomicBoolean.get();
	}

	private static boolean connectedToAnyHost() {
		for (int i = 0; i < hosts.length; i++) {
			String host = hosts[i];
			if (tryConnectingToHost(host)) return true;
		}
		return false;
	}

	private static boolean tryConnectingToHost(String URL) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(URL, 80));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
