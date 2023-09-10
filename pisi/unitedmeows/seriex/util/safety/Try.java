package pisi.unitedmeows.seriex.util.safety;

import java.util.function.Consumer;
import java.util.function.Function;

import pisi.unitedmeows.seriex.Seriex;

public class Try {
	public static <T, R> Function<T, R> safe(Function<T, R> mapper) {
		return t -> {
			try {
				return mapper.apply(t);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		};
	}

	public static <T> Consumer<? super T> safe(Consumer<? super T> action) {
		return t -> {
			try {
				action.accept(t);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		};
	}

	public static void safe(TriedAction action) {
		try {
			action.tried();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void silent(TriedAction action) {
		try {
			action.tried();
		}
		catch (Exception ex) {}
	}

	public static void safe(TriedAction action, String message) {
		try {
			action.tried();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Seriex.get().logger().error(message);
		}
	}

	public static void safe(TriedAction action, TriedAction otherAction) {
		try {
			action.tried();
		}
		catch (Exception ex) {
			try {
				otherAction.tried();
			}
			catch (Exception e) {
				e.addSuppressed(ex);
				e.printStackTrace();
			}
		}
	}

	public static void safe(TriedAction action, TriedAction otherAction, String message) {
		try {
			action.tried();
		}
		catch (Exception ex) {
			try {
				otherAction.tried();
			}
			catch (Exception e) {
				e.addSuppressed(ex);
				e.printStackTrace();
				Seriex.get().logger().error(message);
			}
		}
	}

	@FunctionalInterface
	public interface TriedAction {
		void tried();
	}

}
