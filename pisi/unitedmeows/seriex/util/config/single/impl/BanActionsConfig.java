package pisi.unitedmeows.seriex.util.config.single.impl;

import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "BanActions")
public class BanActionsConfig extends SingleConfig {
	@ConfigField public ConfigValue<Boolean> DISABLE_LOGIN = new ConfigValue<>("login.disable", false);
	@ConfigField public ConfigValue<Boolean> LOGIN_TROLL = new ConfigValue<>("login.troll", true); // only when disable_login is true
	@ConfigField public ConfigValue<Boolean> TROLL_MOTD = new ConfigValue<>("troll.motd", true);
	@ConfigField public ConfigValue<Boolean> CRASH_GAME = new ConfigValue<>("game.crash", true);
	@ConfigField public ConfigValue<Boolean> DISABLE_DISCORD = new ConfigValue<>("discord.actions", true); // disables messaging on discord etc joining vc`s etc...
	@ConfigField public ConfigValue<Boolean> ANNOUNCE_IP_ON_JOIN = new ConfigValue<>("announceiponjoin", false);
}
