package test;

import static java.lang.String.*;
import static java.lang.System.*;
import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.util.math.Primitives.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrimitivesTest {
	private static final Random RANDOM = new Random();

	public static void main(String... args) {
		boolean infinite = false;
		if (infinite) {
			infinite();
		} else {
			once();
		}
	}

	private static void infinite() {
		long hash = 0;
		long offset = 0;
		long calculatedHash;
		String prefix = "1";
		long ms = currentTimeMillis();
		List<Long> coolHashes = new ArrayList<>();
		List<String> info = new ArrayList<>();
		List<String> kekw = new ArrayList<>();
		long ms2 = currentTimeMillis();
		long amount = 0;
		long perSecond_ = 0;
		double perSecond = 0;
		while (true) {
			amount++;
			perSecond_++;
			if (currentTimeMillis() - ms2 > 1000) {
				perSecond += perSecond_ / 1000D;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("total ");
				stringBuilder.append(amount);
				stringBuilder.append(" average ");
				stringBuilder.append(perSecond);
				stringBuilder.append(" total hashes ");
				stringBuilder.append(kekw.size());
				info.add(stringBuilder.toString());
				perSecond_ = 0;
				ms2 = currentTimeMillis();
			}
			if (currentTimeMillis() - ms > 10000) {
				kekw.forEach(logger()::fatal);
				info.forEach(logger()::info);
				logger().fatal("stopping!");
				break;
			}
			hash = RANDOM.nextLong();
			offset = RANDOM.nextLong();
			calculatedHash = hash(hash, offset);
			boolean check = Long.toString(calculatedHash).startsWith(prefix);
			if (check) {
				kekw.add(new StringBuilder().append("found hash at ms " + currentTimeMillis() + " ### hash " + calculatedHash).toString());
				coolHashes.add(calculatedHash);
			}
			logger().debug(format("#1 %d #2 %d", hash, offset));
		}
	}

	private static void once() {
		long hash = 0;
		long offset = 0;
		long calculatedHash;
		String prefix = "21730";
		long ms = currentTimeMillis();
		long ms2 = currentTimeMillis();
		long amount = 0;
		double perSecond = 0;
		List<String> info = new ArrayList<>();
		boolean bruh = false;
		boolean shit = false;
		while (true) {
			amount++;
			if (currentTimeMillis() - ms2 > 500) {
				perSecond += amount / 1000D;
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("total ");
				stringBuilder.append(amount);
				stringBuilder.append(" average ");
				stringBuilder.append(perSecond);
				stringBuilder.append(" ms ");
				stringBuilder.append(currentTimeMillis());
				stringBuilder.append(" total ms ");
				stringBuilder.append(currentTimeMillis() - ms);
				info.add(stringBuilder.toString());
				ms2 = currentTimeMillis();
			}
			if (currentTimeMillis() - ms > 10000) {
				bruh = true;
				break;
			}
			hash = RANDOM.nextLong();
			offset = RANDOM.nextLong();
			calculatedHash = hash(hash, offset);
			boolean check = Long.toString(calculatedHash).startsWith(prefix);
			if (check) {
				info.forEach(logger()::info);
				logger().fatal("found coin hash!");
				logger().fatal(format("gaming hash: %s", calculatedHash));
				logger().fatal(format("hash #1 %d hash #2 %d", hash, offset));
				long prefixAsLong = Long.parseLong(prefix);
				long finalized = hash < 0 ? Math.abs(hash) + prefixAsLong : hash;
				logger().fatal(format("calculated hash: 0x%s%d", prefixAsLong, finalized));
				shit = true;
				break;
			}
			logger().debug(format("#1 %d #2 %d", hash, offset));
		}
		if (!shit) {
			info.forEach(logger()::info);
		}
		if (bruh) {
			logger().fatal(format("couldnt find coin hash (%s, %s)", amount, perSecond));
		}
	}
}
