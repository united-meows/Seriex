package pisi.unitedmeows.seriex.util.config.multi.impl;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.minigames.MinigameManager;
import pisi.unitedmeows.seriex.util.config.multi.MultiConfig;
import pisi.unitedmeows.seriex.util.config.multi.util.ConfigHandler;
import pisi.unitedmeows.seriex.util.config.multi.util.MultiConfigHandler;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Minigame")
public class MinigameConfig extends MultiConfig {
	@ConfigField public ConfigValue<String> WORLD_NAME = new ConfigValue<>("world_name", "empty");
	@ConfigField public ConfigValue<List<Double>> SPAWN_LOCATION = new ConfigValue<>("spawn_location", new ArrayList<>());
	@ConfigField public ConfigValue<List<List<Double>>> ALLOWED_LIMIT = new ConfigValue<>("allowed_limit", new ArrayList<>());
	@ConfigHandler(start = false) public MultiConfigHandler handler = () -> {
		MinigameManager minigameManager = Seriex.get().minigameManager();
		return minigameManager.minigameMap.keySet().stream().toList(); 
	}; 
}
