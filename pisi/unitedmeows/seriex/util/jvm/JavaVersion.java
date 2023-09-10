package pisi.unitedmeows.seriex.util.jvm;

import pisi.unitedmeows.seriex.util.Parser;

/**
 * @author GSON
 */
public final class JavaVersion {
	// Oracle defines naming conventions at http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
	// However, many alternate implementations differ. For example, Debian used 9-debian as the version string

	private static final int majorJavaVersion = determineMajorJavaVersion();

	private static int determineMajorJavaVersion() {
		String javaVersion = System.getProperty("java.version");
		return getMajorJavaVersion(javaVersion);
	}

	// Visible for testing only
	static int getMajorJavaVersion(String javaVersion) {
		int version = parseDotted(javaVersion);
		if (version == -1) {
			version = extractBeginningInt(javaVersion);
		}
		if (version == -1) {
			return 8;  // Choose minimum supported JDK version as default
		}
		return version;
	}

	// Parses both legacy 1.8 style and newer 9.0.4 style
	private static int parseDotted(String javaVersion) {
		String[] parts = javaVersion.split("[._]");
		int firstVer = Parser.parseInt(parts[0], -1);
		if (firstVer == 1 && parts.length > 1) {
			return Parser.parseInt(parts[1], -1);
		} else {
			return firstVer;
		}
	}

	private static int extractBeginningInt(String javaVersion) {
		StringBuilder num = new StringBuilder();
		for (int i = 0; i < javaVersion.length(); ++i) {
			char c = javaVersion.charAt(i);
			if (Character.isDigit(c)) {
				num.append(c);
			} else {
				break;
			}
		}
		return Parser.parseInt(num.toString(), -1);
	}

	/**
	 * @return the major Java version, i.e. '8' for Java 1.8, '9' for Java 9 etc.
	 */
	public static int getMajorJavaVersion() { return majorJavaVersion; }

	/**
	 * @return {@code true} if the application is running on Java 9 or later; and {@code false} otherwise.
	 */
	public static boolean isJava9OrLater() { return majorJavaVersion >= 9; }

	private JavaVersion() {}
}
