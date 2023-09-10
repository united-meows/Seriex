package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Scoreboard")
public class ScoreboardConfig extends SingleConfig {
	@ConfigField public ConfigValue<String> TITLE_PREFIX = new ConfigValue<>("title.prefix", "&7{&5@&8} &5&l");
	@ConfigField public ConfigValue<String> SUBTITLE_PREFIX = new ConfigValue<>("subtitle.prefix", "&8&l>>&7");
}
