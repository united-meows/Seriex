package pisi.unitedmeows.seriex.anticheat;

import java.util.HashMap;
import java.util.Map;

import pisi.unitedmeows.seriex.util.suggestion.ForcedSuggestion;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.Trie;

/**
 * We need this class because of some limitations with enums
 */
public class AnticheatCache {
	public static final Map<String, Anticheat> allPossibleNamesCache = new HashMap<>();
	public static final Trie forcedSuggestion = ForcedSuggestion.createForcedSuggestion();
}
