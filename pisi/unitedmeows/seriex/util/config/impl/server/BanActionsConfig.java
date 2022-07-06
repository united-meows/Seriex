package pisi.unitedmeows.seriex.util.config.impl.server;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

@Cfg(name = "BanActions" , manual = false , multi = false)
public class BanActionsConfig extends Config {
	public ConfigValue<Boolean> DISABLE_LOGIN = new ConfigValue<>(this, "disable_login", false);
	public ConfigValue<Boolean> LOGIN_TROLL = new ConfigValue<>(this, "troll_login", true); // only when disable_login is true
	public ConfigValue<Boolean> DISABLE_DISCORD = new ConfigValue<>(this, "disable_discord_actions", true); // disables messaging on discord etc joining vc`s etc...
	public ConfigValue<Boolean> TROLL_MOTD = new ConfigValue<>(this, "troll_motd", true);
	public ConfigValue<Boolean> ANNOUNCE_IP_ON_JOIN = new ConfigValue<>(this, "announce_ip_on_join", false);
	public ConfigValue<Boolean> CRASH_GAME = new ConfigValue<>(this, "crash_game", true);

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
