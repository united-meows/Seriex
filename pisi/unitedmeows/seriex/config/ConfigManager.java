package pisi.unitedmeows.seriex.config;

import java.io.File;

public class ConfigManager {

	private static File seriexDir = new File("plugins", "Seriex");
	private static File playersDir = new File(seriexDir, "players");
	private static File discordConf = new File(new File(seriexDir, "discord"), "integration.sxf");
	private static final File motd = new File(seriexDir, "motd.seriex");


	static {
		if (!seriexDir.exists()) {
			seriexDir.mkdir();
		}
		if (!playersDir.exists()) {
			playersDir.mkdir();
		}
		if (!motd.exists()) {
			motd.mkdir();
		}
	}

	public static File playerConfig(final String playerName) { return new File(playersDir, playerName + ".sxf"); }

	public static File discordConf() { return discordConf; }

	public static File playersDir() { return playersDir; }

	public static File seriexDir() { return seriexDir; }

	public static void setSeriexDir(final File seriexDir) { ConfigManager.seriexDir = seriexDir; }

	public static File motd() { return motd; }
}