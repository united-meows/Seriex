package test;

import java.security.NoSuchAlgorithmException;

import pisi.unitedmeows.seriex.util.math.Hashing;

public class HashTest {
	public static void main(String... args) throws NoSuchAlgorithmException {
		String test = "asdsadas";
		System.out.println(Hashing.hashedString(test));
	}
}
