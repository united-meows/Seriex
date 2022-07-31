package test.impl;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.timings.TimingsCalculator;
import pisi.unitedmeows.seriex.util.title.AnimatedTitle;

public class TextAnimationTest {
	private static AtomicReference<String[]> troll = new AtomicReference<>(null);
	private static AtomicReference<String[]> troll2 = new AtomicReference<>(null);

	public static void main(String... args) {
		TimingsCalculator.GET.benchmark(() -> {
			String[] text = animateText("Welcome to Seriex!", "Seriex", "&d", "&5&l");
			troll.set(text);
		}, "old text animation");
		TimingsCalculator.GET.benchmark(() -> {
			String[] text = AnimatedTitle.animateText("Welcome to Seriex!", "Seriex", "&d", "&5&l");
			troll2.set(text);
		}, "new text animation");
		boolean areTheAnimationsEqual = Arrays.equals(troll.get(), troll2.get());
		Seriex.logger().debug("Animations are %s", areTheAnimationsEqual ? "equal" : "not equal");
		if (!areTheAnimationsEqual) {
			String seperator = "----------------------------";
			System.out.println(seperator);
			print(troll.get());
			System.out.println(seperator);
			print(troll2.get());
			System.out.println(seperator);
		}
		System.exit(areTheAnimationsEqual ? 0 : -1);
	}

	private static void print(String... str) {
		Arrays.stream(str).forEach(System.out::println);
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
			char[] messageCharacters = message.toCharArray();
			if (messageCharacters[messageCharacters.length - 1] == ' ') {
				continue;
			}
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
