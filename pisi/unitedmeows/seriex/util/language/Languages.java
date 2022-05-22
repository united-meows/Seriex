package pisi.unitedmeows.seriex.util.language;

import java.util.Locale;

public enum Languages {
	ENGLISH("en", Locale.ENGLISH, "🇺🇸"),
	TURKISH("tr", new Locale("tr"), "🇹🇷"),
	RUSSIAN("ru", new Locale("ru"), "🇷🇺"),
	AZERBAIJAN("az", new Locale("az"), "🇦🇿");

	private String languageCode;
	private Locale locale;
	private String unicode;

	Languages(String code, Locale locale, String unicode) {
		this.languageCode = code;
		this.locale = locale;
		this.unicode = unicode;
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
