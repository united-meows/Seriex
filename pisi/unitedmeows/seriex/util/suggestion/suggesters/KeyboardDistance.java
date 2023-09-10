package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisi.unitedmeows.seriex.util.language.Language;

public class KeyboardDistance {

	private static Map<Language, String[]> KEYBOARD_ROWS = new EnumMap<>(Language.class);
	private static final Map<Language, Map<Character, Point>> letterToPosition = new EnumMap<>(Language.class);

	static {
		for (Language language : Language.values()) {
			String[] keyboard_rows = language.keyboard_rows();
			KEYBOARD_ROWS.put(language, keyboard_rows);
			Map<Character, Point> map = new HashMap<>();
			for (int i = 0; i < keyboard_rows.length; i++) {
				final String row = keyboard_rows[i];
				for (int j = 0; j < row.length(); j++) {
					map.put(row.charAt(j), new Point(i, j));
					letterToPosition.put(language, map);
				}
			}
		}
	}

	public static List<Character> getClosestKeys(Language language, char letter) {
		List<Character> closest = new ArrayList<>();
		Point coord = letterToPosition.get(language).get(letter);
		String[] keyboard_row = KEYBOARD_ROWS.get(language);
		int x = (int) coord.getX();
		int y = (int) coord.getY();
		if (x - 1 >= 0 && y < keyboard_row[x - 1].length()) {
			closest.add(keyboard_row[x - 1].charAt(y));
		}
		if (x + 1 < keyboard_row.length && y < keyboard_row[x + 1].length()) {
			closest.add(keyboard_row[x + 1].charAt(y));
		}
		if (y - 1 >= 0) {
			closest.add(keyboard_row[x].charAt(y - 1));
		}
		if (y + 1 < keyboard_row[x].length()) {
			closest.add(keyboard_row[x].charAt(y + 1));
		}
		return closest;
	}

	public static boolean areNeighbours(Language language, char c, char d) {
		return getClosestKeys(language, c).contains(d);
	}

	public static double keyboardDistance(Language language, char letter1, char letter2) {
		Map<Character, Point> map = letterToPosition.get(language);
		return map.get(letter1).distance(map.get(letter2));
	}
}
