package pisi.unitedmeows.seriex.minigames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.reflections.Reflections;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.cache.BasicCache;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.MinigameConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;

public class MinigameManager extends Manager {
	public Map<String, Minigame> minigameMap = new HashMap<>();
	public BasicCache<Minigame[]> minigamesCache = new BasicCache<Minigame[]>().setLocked(true);

	@Override
	public void start(Seriex seriex) {
		try {
			Reflections reflections = new Reflections("pisi.unitedmeows.seriex.minigames.impl");
			for (Class<? extends Minigame> clazz : reflections.getSubTypesOf(Minigame.class)) {
				Minigame minigame = clazz.newInstance();
				minigameMap.put(clazz.getSimpleName(), minigame);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		super.start(seriex);
	}

	@Override
	public void post(Seriex seriex) {
		minigameMap.forEach((string, minigame) -> {
			FileManager fileManager = seriex.get().fileManager();
			MinigameConfig config = (MinigameConfig) fileManager.getConfig(fileManager.MINIGAME);
			minigame.name = config.NAME.value();
			minigame.worldName = config.WORLD_NAME.value();
			List<Double> spawnLocationList = config.SPAWN_LOCATION.value();
			World world = Bukkit.getWorld(minigame.worldName);
			minigame.spawnLocation = new Location(world, spawnLocationList.get(0), spawnLocationList.get(1), spawnLocationList.get(2));
			List<List<Double>> axisLimits = config.ALLOWED_LIMIT.value();
			List<Double> minimumLimits = axisLimits.get(0);
			List<Double> maximumLimits = axisLimits.get(1);
			Location l1 = new Location(world, minimumLimits.get(0), minimumLimits.get(1), minimumLimits.get(2));
			Location l2 = new Location(world, maximumLimits.get(0), maximumLimits.get(1), maximumLimits.get(2));
			minigame.allowedLimit = new AxisBB(l1, l2);
		});
		super.post(seriex);
	}

	public Minigame[] minigames() {
		if (minigamesCache.isLocked()) {
			minigamesCache.set(minigameMap.values().stream().toArray(Minigame[]::new));
			minigamesCache.setLocked(false);
		}
		return minigamesCache.get();
	}
}
