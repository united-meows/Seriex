package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import static pisi.unitedmeows.seriex.util.suggestion.WordList.*;
import static pisi.unitedmeows.seriex.util.suggestion.suggesters.EditDistance.*;

import java.util.Comparator;

public class ProximityComparator implements Comparator<String> {
	private final String string;

	public ProximityComparator(String string) {
		this.string = string;
	}

	@Override
	public final int compare(String str1, String str2) {
		int ed1 = damerauLevenshtein(string, str1);
		int ed2 = damerauLevenshtein(string, str2);
		if (Math.abs(ed1 - ed2) < 1) {
			Integer freq1 = FREQUENCY.get(str1);
			Integer freq2 = FREQUENCY.get(str2);
			if (freq1 == null && freq2 == null) return 0;
			else if (freq1 == null) return 1;
			else if (freq2 == null) return -1;
			return freq2 - freq1;
		}
		if (ed1 > ed2) return 1;
		return -1;
	}
}
