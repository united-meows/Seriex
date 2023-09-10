package pisi.unitedmeows.seriex.util.suggestion;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.filechooser.FileSystemView;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.Parser;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.yystal.parallel.Async;

public class WordList {
	private static final Pattern PATTERN = Pattern.compile("\\p{L}+");
	private static final Pattern NUMBERS_PATTERN = Pattern.compile("\\d");
	private static final Pattern RESOURCE_PATTERN = Pattern.compile(".*");
	private static final Pattern OTHER_PATTERN = Pattern.compile("\0", Pattern.LITERAL);
	public static Map<String, Set<String>> LOWERCASE_WORDS = new HashMap<>();
	public static Map<String, Integer> FREQUENCY = new HashMap<>();
	private static Set<String> strings = new HashSet<>();
	static int totalWords = 0;

	public static void read() {
		try {
			Map<String, Integer> wordCount = new HashMap<>();
			Map<String, Integer> slangCount = new HashMap<>();
			try (ScanResult result = new ClassGraph().acceptPaths("wordlist").scan();
						ResourceList resourcesMatchingPattern = result.getAllResources()) {
				resourcesMatchingPattern.forEach(resource -> {
					String realString = "/" + resource.getPath();
					String[] split = realString.split("/");
					String string = split[split.length - 1];
					String locale = string.substring(0, string.indexOf('.'));
					String extension = string.substring(string.indexOf('.') + 1);
					if (!"words".equals(extension) && !"slang".equals(extension)) {
						return;
					} 
					boolean isWords = "words".equals(extension);
					try (InputStream inputStream = WordList.class.getResourceAsStream(realString)) {
						Set<String> localeSet = new HashSet<>();
						try (BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
							if (isWords) {
								String readLine = "";
								int a = 0;
								while ((readLine = bf.readLine()) != null) {
									if (PATTERN.matcher(readLine).find() && !readLine.contains(" ")) {
										localeSet.add(readLine.toLowerCase(Locale.forLanguageTag(locale)));
										a++;
									}
								}
								wordCount.put(locale, a);
								LOWERCASE_WORDS.put(locale, localeSet);
							} else {
								String readLine = "";
								int a = 0;
								while ((readLine = bf.readLine()) != null) {
									if (PATTERN.matcher(readLine).find() && !readLine.contains(" ")) {
										String lowerCase = readLine.toLowerCase(Locale.forLanguageTag(locale));
										String[] splitMoment = lowerCase.split(":");
										FREQUENCY.put(splitMoment[0], Parser.parseInt(splitMoment[1], 0));
										a++;
									}
									slangCount.put(locale, a);
								}
							}
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
				});
			}
			Language[] values = Language.values();
			for (Language language : values) {
				if (!LOWERCASE_WORDS.containsKey(language.languageCode())) {
					LOWERCASE_WORDS.put(language.languageCode(), new HashSet<>());
				}
			}
			wordCount.forEach((locale, wordAmount) -> {
				Seriex.get().logger().info("Read {} amount of words from locale {}", wordAmount, locale);
			});
			slangCount.forEach((locale, slangAmount) -> {
				Seriex.get().logger().info("Read {} amount of slang from locale {}", slangAmount, locale);
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String removePunctutation(final String input) {
		final StringBuilder builder = new StringBuilder();
		char[] charArray = input.toCharArray();
		for (final char c : charArray) {
			if (Character.isLetterOrDigit(c)) {
				builder.append(Character.isLowerCase(c) ? c : Character.toLowerCase(c));
			}
		}
		return builder.toString();
	}

	public static void swap(char[] arr, int idx1, int idx2) {
		char temp = arr[idx1];
		arr[idx1] = arr[idx2];
		arr[idx2] = temp;
	}

	public static int min(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static double min(double a, double b, double c) {
		return Math.min(Math.min(a, b), c);
	}

	public static float min(float a, float b, float c) {
		return Math.min(Math.min(a, b), c);
	}

	private static AtomicBoolean readAll = new AtomicBoolean(false);
	private static long ms = System.currentTimeMillis();

	// for slangs...
	public static void readBooks(String languageTag) {
		read();
		String desktopPath = String.format("%s\\", FileSystemView.getFileSystemView().getHomeDirectory().toString());
		String writtenFileName = languageTag + ".slang";
		String PATH_TO_BOOKS = desktopPath + "dataset" + "\\" + languageTag;
		String PATH_TO_WRITE = desktopPath + writtenFileName;
		Async.async_loop_condition(() -> {
			Seriex.get().logger().debug("Read {} amount of words... (time passed: {})", totalWords, System.currentTimeMillis() - ms);
		}, 1000L, () -> !readAll.get());
		try (Stream<Path> paths = Files.walk(Paths.get(PATH_TO_BOOKS))) {
			Locale locale = Locale.forLanguageTag(languageTag); // the language tag
			StringBuilder generalBuilder = new StringBuilder();
			Map<String, Integer> freqMap = new HashMap<>();
			paths.filter(f -> f.toFile().isFile()).forEach(pathString -> {
				String string = pathString.toAbsolutePath().toString();
				if (string.endsWith(".txt") && string.contains(languageTag) && !string.contains(writtenFileName)) { // extension
					File source = new File(string);
					try (Scanner scanner = new Scanner(Files.newInputStream(source.toPath()), UTF_8.displayName())) {
						int currentWords = 0;
						scanner.useLocale(locale);
						while (scanner.hasNext()) {
							String next = scanner.next();
							next = removePunctutation(next);
							next = next.toLowerCase(locale);
							next = NUMBERS_PATTERN.matcher(next).replaceAll("");
							next = OTHER_PATTERN.matcher(next).replaceAll("");
							boolean contains = LOWERCASE_WORDS.get(languageTag).contains(next);
							if (next.length() > 2 && !next.contains(" ")) {
								freqMap.put(next, freqMap.getOrDefault(next, 0) + (contains ? 2 : 1));
								strings.add(next);
								currentWords++;
								totalWords++;
							}
						}
						Seriex.get().logger().error("Read {} amount of words from {}!", currentWords, string);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			readAll.set(true);
			strings.forEach((String t) -> generalBuilder.append(String.format("%s:%s%n", t, freqMap.get(t))));
			Files.write(Paths.get(PATH_TO_WRITE), generalBuilder.toString().getBytes(UTF_8));
			Optional<Entry<String, Integer>> max = freqMap.entrySet().stream().max(Entry.comparingByValue());
			if (max.isPresent()) {
				Entry<String, Integer> entry = max.get();
				Seriex.get().logger().info("Most used word: {} -> {}", entry.getKey(), entry.getValue());
			} else {
				Seriex.get().logger().error("There isn't any most used word...?");
			}
			Seriex.get().logger().error("Written {} words! (took {} ms)", strings.size(), System.currentTimeMillis() - ms);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
