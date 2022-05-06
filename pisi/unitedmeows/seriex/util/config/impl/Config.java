package pisi.unitedmeows.seriex.util.config.impl;

import java.io.File;
import java.lang.reflect.Field;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

import pisi.unitedmeows.seriex.Seriex;

public class Config {
	protected String name;
	protected File toWrite;
	protected CommentedConfig config;

	public Config(String name) {
		this.name = name;
		this.config = CommentedConfig.inMemoryConcurrent(); // threadsafe
	}

	public void load() {}

	public void save() {
		TomlWriter writer = new TomlWriter();
		writer.write(config, toWrite, WritingMode.REPLACE);
	}

	public void loadDefaultValues() {}

	public void getAndSet(FileConfig get, CommentedConfig set, String string) {
		set.set(string, get.get(string));
	}

	protected void internalDefaultValues(Class<? extends Config> clazz) {
		try {
			// 🤞 🤞 🤞 🤞 hope this works in java 8 above
			Field[] fields = clazz.getFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getAnnotation(ConfigField.class) != null) {
					ConfigValue fieldValue = (ConfigValue) field.get(ConfigValue.class);
					setValue(fieldValue.key(), fieldValue.value());
				}
			}
		}
		catch (Exception e) {
			Seriex.get().logger().info("Couldnt load %s`s default values?! %s", name, e.getMessage());
		}
	}

	protected void internalLoad(Class<? extends Config> clazz) {
		try (FileConfig fileConfig = FileConfig.of(toWrite)) {
			fileConfig.load(); // this is blocking, could kill async loading...
			try {
				// 🤞 🤞 🤞 🤞 hope this works in java 8 above
				Field[] fields = clazz.getFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					if (field.getAnnotation(ConfigField.class) != null) {
						ConfigValue fieldValue = (ConfigValue) field.get(ConfigValue.class);
						getAndSet(fileConfig, config, fieldValue.key());
					}
				}
			}
			catch (Exception e) {
				Seriex.get().logger().info("Couldnt load %s fields! %s", name, e.getMessage());
			}
		}
	}

	protected <T> T setValue(String key, Object val) {
		T set = config.set(key, val);
		return set;
	}

	protected <T> T getValue(String path) {
		return config.get(path);
	}

	public String name() {
		return name;
	}
}
