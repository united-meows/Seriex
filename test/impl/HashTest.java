package test.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.math.Hashing;
import test.Test;
import test.TestSettings;
import test.TestState;

@TestSettings(hasArguments = false)
public class HashTest extends Test {
	private static final String HASH_TEST_USERNAME = "seriex_hash_test";
	private static final String USERNAME = "ghost2173";
	private static final String WALLET_KEY = "0x2173";
	/*
	 * This hashed string is:
	 * final byte[] bytes = UUID.nameUUIDFromBytes("seriex_hash_test".getBytes(UTF_8)).toString().getBytes(UTF_8)
		Hashing.hashedString("0x2173" + DigestUtils.sha256Hex(bytes))
	 */
	private static final String ORIGINAL_HASH = "f09a318dba12cb55540c887bef3d03ecd01563270f9e8607a8b78b0d619e3dea";

	@Override
	public TestState run() {
		try {
			// TODO add more checks
			byte[] bytes = getBytes(HASH_TEST_USERNAME);
			String stringForHash = getStringForHash(WALLET_KEY, bytes);
			String hashedString = Hashing.hashedString(stringForHash);
			boolean passedFirstCheck = Objects.equals(hashedString, ORIGINAL_HASH);
			if (passedFirstCheck) return TestState.SUCCESS;
			else {
				Seriex.logger().fatal("Original hash:        %s\n", ORIGINAL_HASH);
				Seriex.logger().fatal("New (different) hash: %s\n", hashedString);
				return TestState.FAIL;
			}
		}
		catch (Exception e) {
			message(e);
			return TestState.FATAL_ERROR;
		}
	}

	private static String getStringForHash(String key, byte[] sha256Hex) {
		return key + DigestUtils.sha256Hex(sha256Hex);
	}

	private static byte[] getBytes(String IGN) {
		return UUID.nameUUIDFromBytes(IGN.getBytes(UTF_8)).toString().getBytes(UTF_8);
	}
}
