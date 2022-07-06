package test;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.suggestion.WordList;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.Suggester;
import pisi.unitedmeows.seriex.util.timings.TimingsCalculator;

public class SuggestionTest {
	public static void main(String... args) {
		if (true) {
			WordList.readBooks("tr");
			return;
		}
		TimingsCalculator.GET.benchmark(func -> {
			WordList.read();
		}, "Words");
		String suggestionWord = "merhqba";
		String autocompleteWord = "merhab";
		Suggester suggester = new Suggester(WordList.LOWERCASE_WORDS.get("tr"));
		TimingsCalculator.GET.benchmark(func -> {
			suggester.suggestions(suggestionWord, 50).forEach(Seriex.logger()::fatal);
			Seriex.logger().fatal("------");
			suggester.autocomplete(autocompleteWord, 3).forEach(Seriex.logger()::fatal);
		}, "Suggestions");
	}
}
