package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

// TODO finish
// ^^ this should be finished but i think ghost @ 3 am is trying to tell something
@Cfg(name = "Auth" , manual = false , multi = false)
public class AuthConfig extends Config {
	private static List<String> defaultAllowedCommands = new ArrayList<>();
	static {
		defaultAllowedCommands.add("/login");
		defaultAllowedCommands.add("/register");
		defaultAllowedCommands.add("/log");
		defaultAllowedCommands.add("/reg");
		// for 3 iq 12 yo turks
		defaultAllowedCommands.add("/giris");
		defaultAllowedCommands.add("/kayit");
	}
	@ConfigField
	public ConfigValue<Integer> TIMEOUT = new ConfigValue<>(this, "timeout", 30);
	@ConfigField
	public ConfigValue<String> LOGIN_MSG = new ConfigValue<>(this, "login_msg", "Welcome to the server!!");
	@ConfigField // if last ip == current ip & time since last login smaller than 24 hours login instantly
	public ConfigValue<Boolean> SESSION = new ConfigValue<>(this, "session_system", Boolean.TRUE);
	@ConfigField
	public ConfigValue<List<String>> ALLOWED_COMMANDS = new ConfigValue<>(this, "allowed_commands", defaultAllowedCommands);
	@ConfigField // will teleport back after 1 block of movement
	public ConfigValue<Double> ALLOWED_MOVEMENT_DELTA = new ConfigValue<>(this, "allowed_movement_delta", 1.0);

	public AuthConfig(File toWrite) {
		super("Auth");
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
