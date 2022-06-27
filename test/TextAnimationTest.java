package test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class TextAnimationTest {
	public static void main(String... args) {
		Arrays.stream(animateText("Welcome to Seriex!", "Seriex", "&d", "&5&l")).forEach(frame -> {
			System.err.println(frame);
		});
	}

	private static String[] animateText(String kek, String highlightedWord, String primaryColor, String highlightColor) {
		String cool = "&r&l&k!il&r";
		final String coolSuffix = " " + cool;
		final String coolPrefix = cool + " ";
		char[] charArray = kek.toCharArray();
		Set<String> frames = new LinkedHashSet<>();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			String message = kek.substring(0, i + 1);
			char[] highlightedChars = highlightedWord.toCharArray();
			if (charArray.length - 1 == i) {
				frames.add(primaryColor + kek);
			}
			for (int j = 0; j < highlightedChars.length; j++) {
				char hC = highlightedChars[j];
				String highlightedBefore = primaryColor + highlightedWord.substring(0, j);
				String highlightedChar = highlightColor + highlightedWord.substring(j, j + 1);
				String highlightedAfter = primaryColor + highlightedWord.substring(j + 1);
				String newHighlighted = highlightedBefore + highlightedChar + highlightedAfter;
				String string = i == charArray.length - 1 ? message.replace(highlightedWord, newHighlighted) : message;
				String replace = primaryColor + string;
				frames.add(replace);
			}
		}
		Set<String> realFrames = new LinkedHashSet<>();
		frames.forEach(string -> realFrames.add(coolPrefix + string + coolSuffix));
		return realFrames.stream().toArray(String[]::new);
	}
}
