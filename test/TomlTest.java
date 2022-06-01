package test;

import java.io.File;
import java.util.Arrays;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

import pisi.unitedmeows.seriex.managers.area.areas.Area;

public class TomlTest {
	public static void main(String... args) {
		CommentedConfig config = CommentedConfig.inMemoryConcurrent();
		System.setProperty("nightconfig.preserveInsertionOrder", "true");
		config.set("name", "empty_area");
		config.set("base", "");
		config.set("category", Area.Category.NONE);
		String[] coords_0 = {
			"min", "max", "warp"
		};
		String[] coords_1 = {
			"x", "y", "z"
		};
		Arrays.stream(coords_0).forEach(coord_name -> {
			Arrays.stream(coords_1).forEach(coord -> {
				config.set(coord_name + "." + coord, 2173);
			});
		});
		System.out.println("Config: " + config);
		File configFile = new File("commentedConfig.toml");
		TomlWriter writer = new TomlWriter();
		writer.write(config, configFile, WritingMode.REPLACE);
	}
}
