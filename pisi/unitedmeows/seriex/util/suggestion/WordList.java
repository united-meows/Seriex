package pisi.unitedmeows.seriex.util.suggestion;

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Language;

public class WordList {
	private static final Pattern RESOURCE_PATTERN = Pattern.compile(".*");
	private static final Pattern PATTERN = Pattern.compile("\\p{L}+");
	public static Map<String, Set<String>> LOWERCASE_WORDS = new HashMap<>();
	public static Map<String, Integer> FREQUENCY = new HashMap<>();
	private static Set<String> strings = new HashSet<>();
	static int totalWords = 0;

	public static void read() {
		Reflections reflections = new Reflections("pisi.unitedmeows.seriex.util.suggestion.resources", new ResourcesScanner());
		Map<String, Integer> wordCount = new HashMap<>();
		Map<String, Integer> slangCount = new HashMap<>();
		reflections.getResources(RESOURCE_PATTERN).forEach(tempString -> {
			String realString = "/" + tempString;
			String[] split = realString.split("/");
			String string = split[split.length - 1];
			String locale = string.substring(0, string.indexOf('.'));
			String extension = string.substring(string.indexOf('.') + 1);
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
								FREQUENCY.put(splitMoment[0], Integer.parseInt(splitMoment[1]));
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
		for (Language language : Language.values()) {
			LOWERCASE_WORDS.putIfAbsent(language.languageCode(), new HashSet<>());
		}
		wordCount.forEach((locale, wordAmount) -> {
			Seriex.logger().info("Read %s amount of words from locale %s", wordAmount, locale);
		});
		slangCount.forEach((locale, slangAmount) -> {
			Seriex.logger().info("Read %s amount of slang from locale %s", slangAmount, locale);
		});
	}

	public static String removePunctutation(final String input) {
		final StringBuilder builder = new StringBuilder();
		char[] charArray = input.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			final char c = charArray[i];
			if (Character.isLetterOrDigit(c)) {
				builder.append(Character.isLowerCase(c) ? c : Character.toLowerCase(c));
			}
		}
		return builder.toString();
	}

	static String removeNumbers(char[] ch) {
		int m = 0;
		char[] chr = new char[ch.length];
		char[] k = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
		};
		for (int i = 0; i < ch.length; i++) {
			for (int j = 0; j < k.length; j++) {
				if (ch[i] == k[j]) {
					m--;
					break;
				} else {
					chr[m] = ch[i];
				}
			}
			m++;
		}
		String st = String.valueOf(chr);
		return st;
	}

	public static void transform(File source, String srcEncoding, File target, String tgtEncoding) throws IOException {
		try (
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(source), srcEncoding));
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), tgtEncoding))
		) {
			char[] buffer = new char[16384];
			int read;
			while ((read = br.read(buffer)) != -1) {
				bw.write(buffer, 0, read);
			}
		}
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

	// for slangs...
	private static void readBooks(String languageTag) {
		read();
		String PATH_TO_BOOKS = "";
		String PATH_TO_WRITE = "";
		final Pattern pattern = Pattern.compile("[0-9]");
		try (Stream<Path> paths = Files.walk(Paths.get(PATH_TO_BOOKS))) {
			long ms = System.currentTimeMillis();
			Locale locale = Locale.forLanguageTag(languageTag); // the language tag
			StringBuilder generalBuilder = new StringBuilder();
			Map<String, Integer> freqMap = new HashMap<>();
			paths.filter(f -> f.toFile().isFile()).forEach(pathString -> {
				String string = pathString.toAbsolutePath().toString();
				if (string.endsWith(".txt")) { // extension
					File source = new File(string);
					try (Scanner scanner = new Scanner(new FileInputStream(source), UTF_8.displayName())) {
						scanner.useLocale(locale);
						int currentWords = 0;
						while (scanner.hasNext()) {
							String next = scanner.next();
							boolean stop = false;
							char[] charArray = next.toCharArray();
							for (int i = 0; i < charArray.length; i++) {
								char c = charArray[i];
								if (c == '�') {
									stop = true;
									break;
								}
							}
							if (!stop) {
								next = removePunctutation(next);
								next = next.toLowerCase(locale);
								next = pattern.matcher(next).replaceAll("");
								next = next.replace("\0", "");
								if (next.length() > 2 && !next.contains(" ") && LOWERCASE_WORDS.get(languageTag).contains(next)) {
									freqMap.put(next, freqMap.getOrDefault(next, 0) + 1);
									strings.add(next);
									currentWords++;
								}
							}
						}
						Seriex.logger().debug("Read %s amount of words from %s!", currentWords, string);
						totalWords += currentWords;
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			strings.forEach((String t) -> generalBuilder.append(String.format("%s:%s%n", t, freqMap.get(t))));
			Files.write(Paths.get(PATH_TO_WRITE), generalBuilder.toString().getBytes(UTF_8));
			Seriex.logger().fatal("Written %s words! (took %s ms)", strings.size(), System.currentTimeMillis() - ms);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
