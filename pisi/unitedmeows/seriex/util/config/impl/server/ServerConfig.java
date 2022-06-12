package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.cache.BasicCache;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class ServerConfig extends Config {
	@ConfigField
	public ConfigValue<String> SERVER_NAME = new ConfigValue<>(this, "server.name", "Seriex");
	@ConfigField
	public ConfigValue<String> VERSION = new ConfigValue<>(this, "server.version", "4.0");
	@ConfigField
	public ConfigValue<String> MC_VERSION = new ConfigValue<>(this, "server.mc_version", "1.8.X");
	@ConfigField
	public ConfigValue<String> WORLD_NAME = new ConfigValue<>(this, "server.world", "arterial");
	@ConfigField
	public ConfigValue<Double> WORLD_SPAWN_X = new ConfigValue<>(this, "server.world.spawn_coords.x", 0.0);
	@ConfigField
	public ConfigValue<Double> WORLD_SPAWN_Y = new ConfigValue<>(this, "server.world.spawn_coords.y", 64.0);
	@ConfigField
	public ConfigValue<Double> WORLD_SPAWN_Z = new ConfigValue<>(this, "server.world.spawn_coords.z", 0.0);
	@ConfigField
	public ConfigValue<String> MESSAGE_SUFFIX = new ConfigValue<>(this, "server.msg_suffix", Seriex.colorizeString("&7[&dSer&5iex&7]"));
	@ConfigField
	public ConfigValue<Boolean> LOGGING_ENABLED = new ConfigValue<>(this, "logging.enabled", true);
	@ConfigField
	public ConfigValue<Boolean> VERBOSE_LOGGING = new ConfigValue<>(this, "logging.verbose", true);
	@ConfigField
	public ConfigValue<Boolean> ALLOW_PATCH_BYPASS = new ConfigValue<>(this, "hidden.allow_patch_bypass", true);
	@ConfigField
	public ConfigValue<String> PATCH_BYPASS_MESSAGE = new ConfigValue<>(this, "hidden.patch_bypass_message", "$erieX|2173");
	@ConfigField
	public ConfigValue<String> SERVER_WEBSITE = new ConfigValue<>(this, "server.website", "https://seriex.software");

	public ServerConfig(File toWrite) {
		super("Server");
		this.toWrite = toWrite;
		worldSpawn.setLocked(true);
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}

	private BasicCache<Location> worldSpawn = new BasicCache<>();

	public Location getWorldSpawn() {
		if (worldSpawn.isPresent()) return worldSpawn.get();
		if (worldSpawn.isLocked()) {
			double spawnX = WORLD_SPAWN_X.value();
			double spawnY = WORLD_SPAWN_Y.value();
			double spawnZ = WORLD_SPAWN_Z.value();
			String worldName = WORLD_NAME.value();
			Location value = new Location(Bukkit.getWorld(worldName), spawnX, spawnY, spawnZ);
			worldSpawn.set(value);
			worldSpawn.setLocked(false);
			return value;
		}
		throw new SeriexException("This should never happen?");
	}
}
