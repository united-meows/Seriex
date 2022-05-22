package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import java.util.Comparator;

import pisi.unitedmeows.seriex.util.suggestion.WordList;

public class ProximityComparator implements Comparator<String> {
	private final String string;

	public ProximityComparator(String string) {
		this.string = string;
	}

	@Override
	public int compare(String str1, String str2) {
		int ed1 = EditDistance.damerauLevenshtein(string, str1);
		int ed2 = EditDistance.damerauLevenshtein(string, str2);
		if (Math.abs(ed1 - ed2) < 1) {
			Integer freq1 = WordList.FREQUENCY.get(str1);
			Integer freq2 = WordList.FREQUENCY.get(str2);
			if (freq1 == null && freq2 == null) return 0;
			else if (freq1 == null) return 1;
			else if (freq2 == null) return -1;
			return freq2 - freq1;
		}
		if (ed1 > ed2) return 1;
		return -1;
	}
}
