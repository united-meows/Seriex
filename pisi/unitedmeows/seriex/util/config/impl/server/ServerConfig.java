package pisi.unitedmeows.seriex.util.config.impl.server;

import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

public class ServerConfig extends Config {
	@ConfigField
	private ConfigValue GUILD_ID = new ConfigValue(this, "general.discord.guild_id", "824637279824773231");
	@ConfigField
	private ConfigValue BOT_TOKEN = new ConfigValue(this, "general.discord.bot_token", get().fileManager().PRIVATE);
	@ConfigField
	private ConfigValue SERVER_NAME = new ConfigValue(this, "general.server.name", "Seriex");
	@ConfigField
	private ConfigValue VERSION = new ConfigValue(this, "general.server.version", "4.0");
	@ConfigField
	private ConfigValue MC_VERSION = new ConfigValue(this, "general.server.mc_version", "1.8.X");
	@ConfigField
	private ConfigValue LOGGING_ENABLED = new ConfigValue(this, "general.logging.enabled", true);
	@ConfigField
	private ConfigValue VERBOSE_LOGGING = new ConfigValue(this, "general.logging.verbose", true);
	@ConfigField
	private ConfigValue ALLOW_PATCH_BYPASS = new ConfigValue(this, "general.hidden.allow_patch_bypass", true);
	@ConfigField
	private ConfigValue PATCH_BYPASS_MESSAGE = new ConfigValue(this, "general.hidden.patch_bypass_message", "$erieX|2173");

	public ServerConfig(File toWrite) {
		super("Server");
		this.toWrite = toWrite;
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
