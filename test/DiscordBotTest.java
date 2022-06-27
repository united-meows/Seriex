package test;

import static java.lang.System.*;

import java.io.File;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;

import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.util.config.FileManager;

public class DiscordBotTest {
	public static void main(String... args) {
		setProperty("nightconfig.preserveInsertionOrder", "true");
		FormatDetector.registerExtension("seriex", TomlFormat.instance());
		new DiscordBot(new FileManager(new File("C:\\Users\\EFE\\Desktop\\meow\\seriexbot_test")));
	}
}
