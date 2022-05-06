package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

// TODO save as stelix kekw
public class PlayerConfig extends Config {
	public PlayerConfig(String name, File toWrite) {
		super(name);
		this.toWrite = toWrite;
	}

	// GUEST
	@ConfigField
	public ConfigValue IS_GUEST = new ConfigValue(this, "guest", false); // string
	// DISCORD
	@ConfigField
	public ConfigValue DISCORD_ID = new ConfigValue(this, "discord.id", "???"); // string
	@ConfigField
	public ConfigValue DISCORD_JOIN_DATE = new ConfigValue(this, "discord.join_date", 0L); // ms
	@ConfigField
	public ConfigValue DISCORD_LINK_DATE = new ConfigValue(this, "discord.link_date", 0L); // ms
	@ConfigField
	public ConfigValue DISCORD_JOIN_NICK = new ConfigValue(this, "discord.joined_as", "???"); // username#discriminator
	// INFO
	@ConfigField
	public ConfigValue FIRST_JOIN_DATE = new ConfigValue(this, "info.first_join_date", 0L); // ms
	@ConfigField
	public ConfigValue LAST_JOIN_DATE = new ConfigValue(this, "info.last_join_date", 0L); // ms
	@ConfigField
	public ConfigValue FIRST_IP = new ConfigValue(this, "info.first_ip", ""); // list 
	@ConfigField
	public ConfigValue LAST_IP = new ConfigValue(this, "info.last_ip", ""); // list 
	@ConfigField
	public ConfigValue IP_ADRESSES = new ConfigValue(this, "info.ip_adresses", new ArrayList<String>()); // list 
	// INGAME
	@ConfigField
	public ConfigValue USERNAME = new ConfigValue(this, "ingame.username", "");
	@ConfigField
	public ConfigValue TOKEN = new ConfigValue(this, "ingame.token", "");
	@ConfigField
	public ConfigValue PASSWORD = new ConfigValue(this, "ingame.password", "");
	@ConfigField
	public ConfigValue GOOGLE_AUTH = new ConfigValue(this, "ingame.2FA", "");
	@ConfigField
	public ConfigValue PREMIUM_ACCOUNT = new ConfigValue(this, "ingame.premium", false);
	// INGAME.SETTINGS
	@ConfigField
	public ConfigValue SELECTED_ANTICHEAT = new ConfigValue(this, "ingame.settings.anticheat", "");

	@Override
	public void load() {
		internalLoad(this.getClass());
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this.getClass());
	}

	public List<String> getIPsAsList() {
		return config.get("info.ip_adresses"); // wtf generic magic
	}
}
