package pisi.unitedmeows.seriex.util.config.single.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Server")
public class ServerConfig extends SingleConfig {
	@ConfigField public ConfigValue<String> SERVER_NAME = new ConfigValue<>("server.name", "Seriex");
	@ConfigField public ConfigValue<String> VERSION = new ConfigValue<>("server.version", "4.0");
	@ConfigField public ConfigValue<String> MC_VERSION = new ConfigValue<>("server.mc_version", "1.8.X");
	@ConfigField public ConfigValue<String> WORLD_NAME = new ConfigValue<>("world.name", "arterial");
	@ConfigField public ConfigValue<Double> WORLD_SPAWN_X = new ConfigValue<>("world.spawn_coords.x", 0.0);
	@ConfigField public ConfigValue<Double> WORLD_SPAWN_Y = new ConfigValue<>("world.spawn_coords.y", 64.0);
	@ConfigField public ConfigValue<Double> WORLD_SPAWN_Z = new ConfigValue<>("world.spawn_coords.z", 0.0);
	@ConfigField public ConfigValue<String> MESSAGE_SUFFIX = new ConfigValue<>("msg_suffix", Seriex.colorizeString("&7[&dSer&5iex&7]"));
	@ConfigField public ConfigValue<Boolean> LOGGING_ENABLED = new ConfigValue<>("logging.enabled", true);
	@ConfigField public ConfigValue<Boolean> VERBOSE_LOGGING = new ConfigValue<>("logging.verbose", true);
	@ConfigField public ConfigValue<Boolean> ALLOW_PATCH_BYPASS = new ConfigValue<>("hidden.allow_patch_bypass", true);
	@ConfigField public ConfigValue<String> PATCH_BYPASS_MESSAGE = new ConfigValue<>("hidden.patch_bypass_message", "$erieX|2173");
	@ConfigField public ConfigValue<String> SERVER_WEBSITE = new ConfigValue<>("website", "https://seriex.software");

	private Location worldSpawn;

	public Location getWorldSpawn() {
		if (worldSpawn != null)
			return worldSpawn;
		double spawnX = WORLD_SPAWN_X.value();
		double spawnY = WORLD_SPAWN_Y.value();
		double spawnZ = WORLD_SPAWN_Z.value();
		String worldName = WORLD_NAME.value();
		Location value = new Location(Bukkit.getWorld(worldName), spawnX, spawnY, spawnZ);
		return this.worldSpawn = value;
	}
}
