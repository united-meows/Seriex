package test.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

public class TomlTest {
	public static void main(String... args) {
		CommentedConfig config = CommentedConfig.inMemoryConcurrent();
		System.setProperty("nightconfig.preserveInsertionOrder", "true");
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> min = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			min.add(i);
		}
		List<Integer> max = new ArrayList<>();
		for (int i = 3; i <= 6; i++) {
			min.add(i);
		}
		list.add(min);
		list.add(max);
		config.set("kumalala", list);
		System.out.println("Config: " + config);
		config.get("kumalala");
		File configFile = new File("commentedConfig.toml");
		TomlWriter writer = new TomlWriter();
		writer.write(config, configFile, WritingMode.REPLACE);
	}
}
