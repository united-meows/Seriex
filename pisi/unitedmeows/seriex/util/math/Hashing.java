package pisi.unitedmeows.seriex.util.math;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class Hashing {
	private static final Random RANDOM = new Random();
	private static final char[] hexArray = "0123456789abcdef".toCharArray();

	public static String hashedString(String string) {
		try {
			StringBuilder builder = new StringBuilder();
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
			builder.append(bytesToHex(hash));
			return builder.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
			return "no algo";
		}
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[(bytes.length << 1)];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[(j << 1)] = hexArray[v >>> 4];
			hexChars[(j << 1) + 1] = hexArray[v & 0x0F];
		}
		return String.valueOf(hexChars);
	}

	public static String randomString(final int length) {
		return random(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	}

	public static String random(final int length, final String chars) {
		return random(length, chars.toCharArray());
	}

	public static String random(final int length, final char[] chars) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			stringBuilder.append(chars[RANDOM.nextInt(chars.length)]);
		}
		return stringBuilder.toString();
	}
}
