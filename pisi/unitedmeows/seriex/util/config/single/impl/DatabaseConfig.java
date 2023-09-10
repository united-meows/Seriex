package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Database")
public class DatabaseConfig extends SingleConfig {
	@ConfigField public ConfigValue<String> DATABASE_NAME = new ConfigValue<>("name", "seriex");
}
