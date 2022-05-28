package test;

import java.io.File;
import java.util.Arrays;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

import pisi.unitedmeows.seriex.Seriex;

public class TomlTest {
	public static void main(String... args) {
		CommentedConfig config = CommentedConfig.inMemoryConcurrent();
		System.setProperty("nightconfig.preserveInsertionOrder", "true");
		int[] coords = new int[3];
		coords[0] = 31;
		coords[1] = 21;
		coords[2] = 11;
		config.set("anan", coords);
		System.out.println("Config: " + config);
		int[] get = config.get("anan");
		Arrays.stream(get).forEach(anan -> Seriex.logger().info(anan + ""));
		File configFile = new File("commentedConfig.toml");
		TomlWriter writer = new TomlWriter();
		writer.write(config, configFile, WritingMode.REPLACE);
	}
}
