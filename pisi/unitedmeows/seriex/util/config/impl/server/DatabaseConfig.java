package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

public class DatabaseConfig extends Config {
	public DatabaseConfig(File toWrite) {
		super("Database");
		this.toWrite = toWrite;
	}

	@ConfigField
	public ConfigValue<String> DATABASE_NAME = new ConfigValue<>(this, "name", "seriex");
	@ConfigField
	public ConfigValue<String> DATABASE_USERNAME = new ConfigValue<>(this, "username", "seriex");
	@ConfigField
	public ConfigValue<String> DATABASE_PASSWORD = new ConfigValue<>(this, "password", "seriexdb123");
	@ConfigField
	public ConfigValue<String> DATABASE_HOST = new ConfigValue<>(this, "host", "79.110.234.147");
	@ConfigField
	public ConfigValue<Integer> DATABASE_PORT = new ConfigValue<>(this, "port", 3306);
}
