package pisi.unitedmeows.seriex.util.config.impl.server;

import static com.electronwill.nightconfig.core.CommentedConfig.inMemoryConcurrent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pisi.unitedmeows.seriex.minigames.Minigame;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;
import pisi.unitedmeows.yystal.utils.Pair;

@Cfg(name = "Minigame" , manual = false , multi = true)
public class MinigameConfig extends Config {
	/*
	 * 
	 protected String name, worldName;
	 protected Location spawnLocation;
	 protected AxisBB allowedLimit;
	 * 
	 */
	@ConfigField
	public ConfigValue<String> NAME = new ConfigValue<>(this, "name", "");
	@ConfigField
	public ConfigValue<String> WORLD_NAME = new ConfigValue<>(this, "world_name", "");
	@ConfigField
	public ConfigValue<List<Double>> SPAWN_LOCATION = new ConfigValue<>(this, "spawn_location", new ArrayList<>());
	@ConfigField
	public ConfigValue<List<List<Double>>> ALLOWED_LIMIT = new ConfigValue<>(this, "allowed_limit", new ArrayList<>());

	public MinigameConfig(File toWrite, String extension, Minigame... minigames) {
		super("Minigame", false, ConfigType.MULTIPLE, toWrite);
		Arrays.stream(minigames).forEach(minigame -> {
			File file = new File(String.format("%s/%s%s", toWrite, minigame.name, extension));
			configs.put(minigame.name, new Pair<>(file, inMemoryConcurrent()));
		});
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
