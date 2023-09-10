package pisi.unitedmeows.seriex.util;

import java.util.HashMap;
import java.util.Map;

public class Parser {

	private static final Map<String, Integer> INT_MAP = new HashMap<>();
	private static final Map<String, Double> DOUBLE_MAP = new HashMap<>();
	private static final Map<String, Long> LONG_MAP = new HashMap<>();
	public static final Map<String, Boolean> BOOLEAN_VALUES = Create.create(new HashMap<>(), map -> {
		map.put("enable", true);
		map.put("true", true);
		map.put("on", true);
		map.put("1", true);
		map.put("t", true);

		map.put("disable", false);
		map.put("false", false);
		map.put("off", false);
		map.put("0", false);
		map.put("f", false);
	});

	public static int parseInt(String value, int defaultValue) {
		return INT_MAP.computeIfAbsent(value, calculatedValue -> {
			try {
				return Integer.parseInt(calculatedValue);
			}
			catch (NumberFormatException exception) {
				return defaultValue;
			}
		});
	}

	public static long parseLong(String value, long defaultValue) {
		return LONG_MAP.computeIfAbsent(value, calculatedValue -> {
			try {
				return Long.parseLong(calculatedValue);
			}
			catch (NumberFormatException exception) {
				return defaultValue;
			}
		});
	}

	public static double parseDouble(String value, double defaultValue) {
		return DOUBLE_MAP.computeIfAbsent(value, calculatedValue -> {
			try {
				return Double.parseDouble(calculatedValue);
			}
			catch (NumberFormatException exception) {
				return defaultValue;
			}
		});
	}

	public static Boolean parseBoolean(String value, Boolean defaultValue) {
		return BOOLEAN_VALUES.getOrDefault(value, defaultValue);
	}
}
