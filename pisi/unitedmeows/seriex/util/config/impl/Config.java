package pisi.unitedmeows.seriex.util.config.impl;

import static com.electronwill.nightconfig.core.CommentedConfig.*;
import static com.electronwill.nightconfig.core.io.WritingMode.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;
import java.lang.reflect.Field;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlWriter;

public class Config {

	protected String name;
	protected File toWrite;
	protected CommentedConfig config;
	public boolean manual;

	public Config(String name) {
		this(name, false);
	}

	public Config(String name, boolean manual) {
		this.name = name;
		this.config = inMemoryConcurrent(); // threadsafe
		this.manual = manual;
	}

	public void load() {}

	public void save() {
		if (!manual) {
			new TomlWriter().write(config, toWrite, REPLACE);
		}
	}

	public void loadDefaultValues() {}

	public void getAndSet(FileConfig get, CommentedConfig set, String string) {
		set.set(string, get.get(string));
	}

	protected void internalDefaultValues(Object o) {
		if (!manual) {
			try {
				// 🤞 🤞 🤞 🤞 hope this works in java 8 above
				Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					field.setAccessible(true);
					if (field.getAnnotation(ConfigField.class) != null) {
						ConfigValue fieldValue = (ConfigValue) field.get(o);
						setValue(fieldValue.key(), fieldValue.value());
					}
				}
			}
			catch (Exception e) {
				logger().info("Couldnt load %s`s default values?! %s", name, e.getMessage());
			}
		}
	}

	protected void internalLoad(Object o) {
		try (FileConfig fileConfig = FileConfig.of(toWrite)) {
			fileConfig.load(); // this is blocking, could kill async loading...
			if (!manual) {
				try {
					// 🤞 🤞 🤞 🤞 hope this works in java 8 above
					Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						Field field = fields[i];
						field.setAccessible(true);
						if (field.getAnnotation(ConfigField.class) != null) {
							ConfigValue fieldValue = (ConfigValue) field.get(ConfigValue.class);
							getAndSet(fileConfig, config, fieldValue.key());
						}
					}
				}
				catch (Exception e) {
					logger().info("Couldnt load %s fields! %s", name, e.getMessage());
				}
			}
		}
	}

	public <T> T setValue(String key, Object val) {
		T set = config.set(key, val);
		return set;
	}

	public <T> T getValue(String path) {
		return config.get(path);
	}

	public String name() {
		return name;
	}
}
