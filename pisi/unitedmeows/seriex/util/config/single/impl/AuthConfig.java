package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Auth")
public class AuthConfig extends SingleConfig {
	@ConfigField public ConfigValue<Integer> TIMEOUT = new ConfigValue<>("timeout", 30);
	@ConfigField public ConfigValue<Boolean> SESSION = new ConfigValue<>("session_system", Boolean.TRUE);
	@ConfigField public ConfigValue<Double> ALLOWED_MOVEMENT_DELTA = new ConfigValue<>("allowed_movement_delta", 1.0);
}
