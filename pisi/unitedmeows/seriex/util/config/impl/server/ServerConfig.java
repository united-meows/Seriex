package pisi.unitedmeows.seriex.util.config.impl.server;

import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

public class ServerConfig extends Config {
	@ConfigField
	public ConfigValue GUILD_ID = new ConfigValue(this, "discord.guild_id", "824637279824773231");
	@ConfigField
	public ConfigValue BOT_TOKEN = new ConfigValue(this, "discord.bot_token", get().fileManager().PRIVATE);
	@ConfigField
	public ConfigValue MAINTAINER_NAME = new ConfigValue(this, "discord.maintainer_name", "$$$#0707"); // :D
	@ConfigField
	public ConfigValue SERVER_NAME = new ConfigValue(this, "server.name", "Seriex");
	@ConfigField
	public ConfigValue VERSION = new ConfigValue(this, "server.version", "4.0");
	@ConfigField
	public ConfigValue MC_VERSION = new ConfigValue(this, "server.mc_version", "1.8.X");
	@ConfigField
	public ConfigValue MESSAGE_SUFFIX = new ConfigValue(this, "server.msg_suffix", Seriex.get().colorizeString("&7[&dSer&5iex&7]"));
	@ConfigField
	public ConfigValue LOGGING_ENABLED = new ConfigValue(this, "logging.enabled", true);
	@ConfigField
	public ConfigValue VERBOSE_LOGGING = new ConfigValue(this, "logging.verbose", true);
	@ConfigField
	public ConfigValue ALLOW_PATCH_BYPASS = new ConfigValue(this, "hidden.allow_patch_bypass", true);
	@ConfigField
	public ConfigValue PATCH_BYPASS_MESSAGE = new ConfigValue(this, "hidden.patch_bypass_message", "$erieX|2173");
	@ConfigField
	public ConfigValue DATABASE_NAME = new ConfigValue(this, "database.name", "seriex");
	@ConfigField
	public ConfigValue DATABASE_USERNAME = new ConfigValue(this, "database.username", "seriex");
	@ConfigField
	public ConfigValue DATABASE_PASSWORD = new ConfigValue(this, "database.password", "seriexdb123");
	@ConfigField
	public ConfigValue DATABASE_HOST = new ConfigValue(this, "database.host", "79.110.234.147");
	@ConfigField
	public ConfigValue DATABASE_PORT = new ConfigValue(this, "database.port", "3306");

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
