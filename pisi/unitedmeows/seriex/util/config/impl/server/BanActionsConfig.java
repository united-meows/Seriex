package pisi.unitedmeows.seriex.util.config.impl.server;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

@Cfg(name = "BanActions" , manual = false , multi = false)
public class BanActionsConfig extends Config {
	@ConfigField
	public ConfigValue<Boolean> DISABLE_LOGIN = new ConfigValue<>(this, "login.disable", false);
	@ConfigField
	public ConfigValue<Boolean> LOGIN_TROLL = new ConfigValue<>(this, "login.troll", true); // only when disable_login is true
	@ConfigField
	public ConfigValue<Boolean> TROLL_MOTD = new ConfigValue<>(this, "troll.motd", true);
	@ConfigField
	public ConfigValue<Boolean> CRASH_GAME = new ConfigValue<>(this, "game.crash", true);
	@ConfigField
	public ConfigValue<Boolean> DISABLE_DISCORD = new ConfigValue<>(this, "discord.actions", true); // disables messaging on discord etc joining vc`s etc...
	@ConfigField
	public ConfigValue<Boolean> ANNOUNCE_IP_ON_JOIN = new ConfigValue<>(this, "announceiponjoin", false);

	public BanActionsConfig() {
		super("BanActions");
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
