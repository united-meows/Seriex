package pisi.unitedmeows.seriex.util.language;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public enum Languages {
	ENGLISH(Locale.ENGLISH, 55356, 56826, 55356, 56824),
	TURKISH(new Locale("tr"), 55356, 56825, 55356, 56823),
	RUSSIAN(new Locale("ru"), 55356, 56823, 55356, 56826),
	AZERBAIJAN(new Locale("az"), 55356, 56806, 55356, 56831);

	private String languageCode;
	private Locale locale;
	private String unicode;
	private Function<int[], String> function = array -> {
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(array).forEach(number -> stringBuilder.append((char) number));
		return stringBuilder.toString();
	};

	Languages(Locale locale, int... unicode) {
		this.languageCode = locale.toString();
		this.locale = locale;
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
}
