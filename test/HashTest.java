package test;

import java.security.NoSuchAlgorithmException;

import pisi.unitedmeows.seriex.util.math.Hashing;

public class HashTest {
	public static void main(String... args) throws NoSuchAlgorithmException {
		for (int i = 0; i < 100_000; i++) {
			if (i % 2 != 0) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(i);
				stringBuilder.append(" ");
				int firstMethod = i - (i >>> 1);
				int secondMethod = (i + 1) / 2;
				int thirdMethod = i - (i >> 1);
				stringBuilder.append(firstMethod);
				stringBuilder.append(" ");
				stringBuilder.append(secondMethod);
				stringBuilder.append(" ");
				stringBuilder.append(thirdMethod);
				System.out.println(stringBuilder.toString());
			}
		}
		// 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
		// 16
		// 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
		//
		// 1 -> 1 (0)
		// 3 -> 2 (1)
		// 5 -> 3 (2)
		// 7 -> 4 (3)
		// 9 -> 5 (4)
		// 11 -> 6 (5)
		String test = "asdsadas";
		System.out.println(Hashing.hashedString(test));
	}
}
