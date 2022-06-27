package pisi.unitedmeows.seriex.util;

import java.util.function.Consumer;

import pisi.unitedmeows.seriex.Seriex;

public class Try {
	public static <T> Consumer<? super T> safe(Consumer<? super T> action, String errorMessage) {
		return t -> {
			try {
				action.accept(t);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				Seriex.get().logger().fatal(errorMessage);
			}
		};
	}

	public static <T> Consumer<? super T> safe(Consumer<? super T> action, String errorMessage, Object... args) {
		return t -> {
			try {
				action.accept(t);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				if (args != null && args.length != 0) {
					Seriex.get().logger().fatal(errorMessage, args);
				} else {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("(Formatting failed!!) ");
					stringBuilder.append(errorMessage);
					Seriex.get().logger().fatal(stringBuilder.toString());
				}
			}
		};
	}
}
