package pisi.unitedmeows.seriex.util;

public record Pair<K, V>(K key, V value) {
	public static <K, V> Pair<K, V> of(K key, V value) {
		return new Pair<>(key, value);
	}

	@Override
	public String toString() {
		return "(" + key + ", " + value + ")";
	}
}
