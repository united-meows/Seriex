package test;

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
		min.add(1);
		min.add(2);
		min.add(3);
		List<Integer> max = new ArrayList<>();
		max.add(4);
		max.add(5);
		max.add(6);
		list.add(min);
		list.add(max);
		config.set("kumalala", list);
		System.out.println("Config: " + config);
		File configFile = new File("commentedConfig.toml");
		TomlWriter writer = new TomlWriter();
		writer.write(config, configFile, WritingMode.REPLACE);
	}
}
