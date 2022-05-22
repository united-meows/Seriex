package pisi.unitedmeows.seriex.util.suggestion.suggesters;

import java.util.*;

import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.suggestion.WordList;

public class Suggester {
	private static final char WILDCARD = '?';
	public Trie trie;

	public Suggester(Collection<String> words) {
		trie = new Trie(words);
	}

	private static int maxEditDistance(String str) {
		if (str.length() <= 4) return 1;
		if (str.length() <= 12) return 2;
		return 3;
	}

	public void addWildcards(int editsLeft, String str, int idx, Set<String> set) {
		if (idx > str.length() || editsLeft == 0) {
			set.add(str);
			return;
		}
		StringBuilder sb = null;
		addWildcards(editsLeft, str, idx + 1, set);
		sb = new StringBuilder(str);
		addWildcards(editsLeft - 1, sb.insert(idx, WILDCARD).toString(), idx + 1, set);
		if (idx != str.length()) {
			sb = new StringBuilder(str);
			addWildcards(editsLeft - 1, sb.deleteCharAt(idx).toString(), idx + 1, set);
			sb = new StringBuilder(str);
			sb.setCharAt(idx, WILDCARD);
			addWildcards(editsLeft - 1, sb.toString(), idx + 1, set);
			char[] arr = str.toCharArray();
			if (idx != str.length() - 1) {
				WordList.swap(arr, idx, idx + 1);
			}
			addWildcards(editsLeft - 1, new String(arr), idx + 2, set);
		}
	}

	public Set<String> getCandidates(String str, int ed) {
		Set<String> candidates = new HashSet<>();
		addWildcards(ed, str, 0, candidates);
		return candidates;
	}

	/**
	 * max is default 5
	 */
	public List<String> suggestions(String str, int max) {
		List<String> suggestions = new GlueList<>();
		for (String s : getCandidates(str, maxEditDistance(str))) {
			suggestions.addAll(trie.wildcardMatches(s));
		}
		suggestions = new GlueList<>(new HashSet<>(suggestions));
		Collections.sort(suggestions, new ProximityComparator(str));
		if (suggestions.size() >= max) return suggestions.subList(0, max);
		return suggestions;
	}

	/**
	 * max is default 5
	 */
	public List<String> autocomplete(String str, int max) {
		List<String> suggestions = trie.prefixedWords(str);
		Collections.sort(suggestions, (arg0, arg1) -> {
			Integer freq1 = WordList.FREQUENCY.get(arg0);
			Integer freq2 = WordList.FREQUENCY.get(arg1);
			if (freq1 == null && freq2 == null) return 0;
			else if (freq1 == null) return 1;
			else if (freq2 == null) return -1;
			return freq2 - freq1;
		});
		if (suggestions.size() >= max) return suggestions.subList(0, max);
		return suggestions;
	}
}
