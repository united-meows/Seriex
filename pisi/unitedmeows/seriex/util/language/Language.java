package pisi.unitedmeows.seriex.util.language;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public enum Language {
	ENGLISH(Locale.ENGLISH, 0b1, 55356, 56826, 55356, 56824),
	TURKISH(new Locale("tr"), 0b10, 55356, 56825, 55356, 56823),
	RUSSIAN(new Locale("ru"), 0b100, 55356, 56823, 55356, 56826),
	AZERBAIJAN(new Locale("az"), 0b1000, 55356, 56806, 55356, 56831);

	private String languageCode;
	private Locale locale;
	private String unicode;
	private int id;
	private Function<int[], String> function = array -> {
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(array).forEach(number -> stringBuilder.append((char) number));
		return stringBuilder.toString();
	};

	Language(Locale locale, int id, int... unicode) {
		this.languageCode = locale.toString();
		this.locale = locale;
		this.id = id;
		this.unicode = function.apply(unicode);
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

	public static boolean isLanguageSelected(int totalID, int id) {
		return (totalID & id) != 0;
	}

	public boolean isLanguageSelected(int totalID) {
		return isLanguageSelected(totalID, id);
	}
}
