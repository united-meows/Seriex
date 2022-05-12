package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

public class BanActionsConfig extends Config {
	public ConfigValue DISABLE_LOGIN = new ConfigValue(this, "disable_login", true);
	public ConfigValue DISABLE_DISCORD = new ConfigValue(this, "disable_discord_actions", true); // disables messaging on discord etc joining vc`s etc...
	// disable login makes all of the below useless
	public ConfigValue ANNOUNCE_IP_ON_JOIN = new ConfigValue(this, "announce_ip_on_join", false);
	public ConfigValue DISABLE_COMMANDS = new ConfigValue(this, "disable_cmd", true); // disables every command 
	public ConfigValue DISABLE_CHAT = new ConfigValue(this, "disable_chat", true);
	public ConfigValue DISABLE_MOVEMENT = new ConfigValue(this, "disable_movement", true); // pretty ez to understand
	public ConfigValue CRASH_GAME = new ConfigValue(this, "crash_game", true);
	// how it works is we send:
	// an invalid C0F packet
	// an invalid explosion packet
	// an invalid position packet
	// send invalid block change
	// to crash the game

	public BanActionsConfig(File toWrite) {
		super("BanActions");
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
