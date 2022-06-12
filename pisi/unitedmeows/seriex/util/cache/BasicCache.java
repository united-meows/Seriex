package pisi.unitedmeows.seriex.util.cache;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicCache<X> {
	protected Supplier<X> cachedValue;
	protected boolean locked;
	protected X cached;

	public BasicCache() {
		this.cachedValue = () -> null;
	}

	public BasicCache(X value) {
		this.cachedValue = () -> value;
		this.cached = value;
	}

	public BasicCache(Supplier<X> getter) {
		this.cachedValue = getter;
	}

	public BasicCache<X> set(X value) {
		this.cached = value;
		return this;
	}

	public X get() {
		if (isPresent()) return cached;
		return null;
	}

	public boolean isPresent() {
		if (cached == null && !locked) {
			cached = cachedValue.get();
		}
		return cached != null;
	}

	public boolean computeIfPresent(Consumer<X> consumer) {
		if (isPresent()) {
			consumer.accept(cached);
			return true;
		}
		return false;
	}

	public <Y> Y returnIfPresent(Function<X, Y> function, Y defaultValue) {
		if (isPresent()) return function.apply(cached);
		return defaultValue;
	}

	/**
	 * disables .get if the operation is heavy
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}
}
