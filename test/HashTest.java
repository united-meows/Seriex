package test;

import static java.nio.charset.StandardCharsets.*;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import pisi.unitedmeows.seriex.util.math.Hashing;

public class HashTest {
	public static void main(String... args) throws NoSuchAlgorithmException {
		final byte[] bytes = UUID.nameUUIDFromBytes("ghost2173".getBytes(UTF_8)).toString().getBytes(UTF_8);
		System.out.println("0x2173" + DigestUtils.sha256Hex(bytes));
		String test = "asdsadas";
		System.out.println(Hashing.hashedString(test));
	}
}
