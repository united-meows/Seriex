package pisi.unitedmeows.seriex.util.suggestion;

import pisi.unitedmeows.seriex.util.suggestion.suggesters.Trie;

public class ForcedSuggestion {	
	public static Trie createForcedSuggestion(String... keywordsToLookOutFor) {
		Trie trie = new Trie();
		for(String keywords : keywordsToLookOutFor) {
			trie.add(keywords);
		}
		return trie;
	}
	
	public static Trie createForcedSuggestion() {
		return new Trie();
	}
}
