package pisi.unitedmeows.seriex.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Create {
	private Create() {}

	public static <T> T create(Supplier<T> supplier) {
		return supplier.get();
	}

	public static <T> T create(T object, Consumer<T> consumer) {
		consumer.accept(object);
		return object;
	}
}
