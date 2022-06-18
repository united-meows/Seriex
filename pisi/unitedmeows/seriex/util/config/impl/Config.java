package pisi.unitedmeows.seriex.util.config.impl;

import static com.electronwill.nightconfig.core.CommentedConfig.*;
import static com.electronwill.nightconfig.core.io.WritingMode.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlWriter;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;
import pisi.unitedmeows.yystal.utils.Pair;

// TODO make toWrite used in a constructor here
public class Config {
	public String name;
	public File toWrite;
	public CommentedConfig config;
	public boolean manual;
	public ConfigType configType;
	/**
	 * Should only be used while configType is MULTIPLE
	 */
	protected Map<String, Pair<File, CommentedConfig>> configs;
	protected File parentDirectory;

	public Config(String name) {
		this(name, false, ConfigType.SINGLE, null);
	}

	public Config(String name, boolean manual) {
		this(name, manual, ConfigType.SINGLE, null);
	}

	public Config(String name, ConfigType type, File parentDirectory) {
		this(name, false, type, parentDirectory);
	}

	public Config(String name, boolean manual, ConfigType type, File parentDirectory) {
		this.name = name;
		this.config = inMemoryConcurrent();
		this.manual = manual;
		this.configType = type;
		if (type == ConfigType.MULTIPLE) {
			configs = new HashMap<>();
			this.parentDirectory = parentDirectory;
		}
	}

	public void load() {}

	public void save() {
		if (!manual) {
			if (hasMultiple()) {
				Future<Boolean> future = Async.async(() -> {
					configs.forEach((name, pair) -> new TomlWriter().write(pair.item2(), pair.item1(), REPLACE));
					return Boolean.FALSE;
				});
				Seriex.get().futureManager().addFuture(future);
			} else {
				new TomlWriter().write(config, toWrite, REPLACE);
			}
		}
	}

	public void loadDefaultValues() {}

	public void getAndSet(FileConfig get, CommentedConfig set, String string) {
		set.set(string, get.get(string));
	}

	protected void internalDefaultValues(Object o) {
		if (hasMultiple()) {
			configs.forEach((name, pair) -> internalDefaultValues0(o, pair.item2()));
		} else {
			internalDefaultValues0(o, config);
		}
	}

	protected void internalLoad(Object o) {
		if (hasMultiple()) {
			configs.forEach((name, pair) -> internalLoad0(o, pair.item1(), pair.item2()));
		} else {
			internalLoad0(o, toWrite, config);
		}
	}

	private void internalDefaultValues0(Object o, CommentedConfig commentedConfig) {
		if (!manual) {
			try {
				// 🤞 🤞 🤞 🤞 hope this works in java 8 above
				Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					field.setAccessible(true);
					if (field.getAnnotation(ConfigField.class) != null) {
						ConfigValue<?> fieldValue = (ConfigValue) field.get(o);
						setValue(fieldValue.key(), fieldValue.value(), commentedConfig);
					}
				}
			}
			catch (Exception e) {
				logger().info("Couldnt load %s`s default values?! %s", name, e.getMessage());
			}
		}
	}

	private void internalLoad0(Object o, File file, CommentedConfig config) {
		try (FileConfig fileConfig = FileConfig.of(file)) {
			fileConfig.load(); // this is blocking, could kill async loading...
			if (!manual) {
				try {
					// 🤞 🤞 🤞 🤞 hope this works in java 8 above
					Field[] fields = ((Class<? extends Config>) o.getClass()).getDeclaredFields();
					for (int i = 0; i < fields.length; i++) {
						Field field = fields[i];
						field.setAccessible(true);
						if (field.getAnnotation(ConfigField.class) != null) {
							ConfigValue<?> fieldValue = (ConfigValue) field.get(o);
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

	public <T> T setValue(String key, Object val, CommentedConfig config) {
		T set = config.set(key, val);
		return set;
	}

	public <T> T getValue(String path, CommentedConfig config) {
		return config.get(path);
	}

	public <T> T getValue(String path) {
		if (hasMultiple()) throw new SeriexException("Config type isnt multiple! Cant invoke getValue(String path)...");
		return getValue(path, config);
	}

	public String name() {
		return name;
	}

	public boolean hasMultiple() {
		return configType == ConfigType.MULTIPLE;
	}

	public Map<String, Pair<File, CommentedConfig>> getConfigs() {
		if (hasMultiple()) return configs;
		else throw new SeriexException("Config type isnt multiple! Cant invoke getConfigs...");
	}

	public File getParentDirectory() {
		if (hasMultiple()) return parentDirectory;
		else throw new SeriexException("Config type isnt multiple! Cant invoke getParentDirectory...");
	}

	public enum ConfigType {
		MULTIPLE,
		SINGLE;
	}
}
