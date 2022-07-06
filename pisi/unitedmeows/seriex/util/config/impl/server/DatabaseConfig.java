package pisi.unitedmeows.seriex.util.config.impl.server;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

@Cfg(name = "Database" , manual = false , multi = false)
public class DatabaseConfig extends Config {
	public DatabaseConfig() {
		super("Database");
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

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
