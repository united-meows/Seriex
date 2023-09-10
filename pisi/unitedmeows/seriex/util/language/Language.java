package pisi.unitedmeows.seriex.util.language;

import static pisi.unitedmeows.seriex.util.suggestion.WordList.LOWERCASE_WORDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.Suggester;

public enum Language {
	ENGLISH(Locale.ENGLISH , new String[] {
				"1234567890-+", "qwertyuiop[]", "asdfghjkl;'\\", "zxcvbnm,./"
	} , 0b1 , new int[] {
				55356, 56826, 55356, 56824
	} , "4cac9774da1217248532ce147f7831f67a12fdcca1cf0cb4b3848de6bc94b4"),
	TURKISH(Locale.of("tr") , new String[] {
				"123456890*-", "qwertyuıopğü", "asdfghjklşi,", "zxcvbnmöç."
	} , 0b10 , new int[] {
				55356, 56825, 55356, 56823
	} , "6bbeaf52e1c4bfcd8a1f4c6913234b840241aa48829c15abc6ff8fdf92cd89e"),
	RUSSIAN(Locale.of("ru") , new String[] {
				"1234567890-=", "йцукенг", "фывапролджэ", "ячсмитьбю."
	} , 0b100 , new int[] {
				55356, 56823, 55356, 56826
	} , "16eafef980d6117dabe8982ac4b4509887e2c4621f6a8fe5c9b735a83d775ad");

	private final String languageCode;
	private final Locale locale;
	private final String unicode;
	private final Suggester suggester;
	private final String[] keyboard_rows;
	private final int id;
	private final String headData;

	Language(Locale locale, String[] keyboard_row, int id, int[] unicode, String headData) {
		this.languageCode = locale.toString();
		Set<String> words = LOWERCASE_WORDS.get(languageCode);
		if (words != null && !words.isEmpty()) {
			this.suggester = new Suggester(words);
		} else {
			this.suggester = null;
			Seriex.get().logger().warn("No suggester is available for language {}", name());
		}
		this.locale = locale;
		this.id = id;
		this.keyboard_rows = keyboard_row;
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(unicode).forEach((int number) -> stringBuilder.append((char) number));
		this.unicode = stringBuilder.toString();
		this.headData = headData;
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

	public String[] keyboard_rows() {
		return keyboard_rows;
	}

	public int mask() {
		return id;
	}

	public String headData() {
		return headData;
	}

	public static Language languageFromID(int id) {
		return values()[(int) (Math.log(id) / Math.log(2))];
	}

	public static List<Language> getLanguages(int id) {
		List<Language> languages = new ArrayList<>();
		Language[] values = values();
		for (Language language : values) {
			if (isLanguageSelected(id, language.id)) {
				languages.add(language);
			}
		}
		return languages;
	}

	public Suggester suggester() { return suggester; }

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
