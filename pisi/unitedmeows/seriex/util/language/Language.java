package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.util.suggestion.WordList.LOWERCASE_WORDS;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.Suggester;

public enum Language {
	ENGLISH(Locale.ENGLISH, 0b1, 55356, 56826, 55356, 56824),
	TURKISH(new Locale("tr"), 0b10, 55356, 56825, 55356, 56823),
	RUSSIAN(new Locale("ru"), 0b100, 55356, 56823, 55356, 56826),
	AZERBAIJAN(new Locale("az"), 0b1000, 55356, 56806, 55356, 56831);

	private final String languageCode;
	private final Locale locale;
	private final String unicode;
	private final Suggester suggester;
	private final int id;

	Language(Locale locale, int id, int... unicode) {
		this.languageCode = locale.toString();
		Set<String> words = LOWERCASE_WORDS.get(languageCode);
		if (words != null && !words.isEmpty()) {
			this.suggester = new Suggester(words);
		} else {
			this.suggester = null;
			Seriex.logger().warn("No suggester is available for language %s", name());
		}
		this.locale = locale;
		this.id = id;
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(unicode).forEach((int number) -> stringBuilder.append((char) number));
		this.unicode = stringBuilder.toString();
	}

	public Locale locale() {
		return locale;
	}

	public String unicode() {
		return unicode;
	}

	public String languageCode() {
		return languageCode;
	}

	public int mask() {
		return id;
	}

	public static Language getLanguage(int id) {
		return values()[(int) (Math.log(id) / Math.log(2))];
	}

	public Suggester getSuggester() {
		return suggester;
	}

	public static boolean isLanguageSelected(int totalID, int id) {
		return (totalID & id) != 0;
	}

	public boolean isLanguageSelected(int totalID) {
		return isLanguageSelected(totalID, id);
	}

	public static Language fromCode(String code, Language default_) {
		Language foundLanguage = default_;
		for (Language language : Language.values()) {
			if (language.languageCode().equalsIgnoreCase(code)) {
				foundLanguage = language;
				break;
			}
		}
		return foundLanguage;
	}
}
