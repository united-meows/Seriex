package pisi.unitedmeows.seriex.managers.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

import dev.derklaro.reflexion.Reflexion;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.util.config.multi.impl.MinigameConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;

public class MinigameManager extends Manager {
	public Map<String, Minigame> minigameMap = new HashMap<>();

	@Override
	public void start(Seriex seriex) {
		try (ScanResult result = new ClassGraph().enableAllInfo().acceptPackages("pisi.unitedmeows.seriex.managers.minigames.impl").scan(4)) {
			result.getAllClasses().filter(f -> f.extendsSuperclass(Minigame.class)).forEach(classInfo -> {
				Class<? extends Minigame> minigameKlass = (Class<Minigame>) classInfo.loadClass();
				Minigame minigame = Reflexion.on(minigameKlass).findConstructor().orElseThrow().<Minigame>invoke().get();
				minigameMap.put(minigame.name = minigameKlass.getSimpleName(), minigame);
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void post(Seriex seriex) {
		ServerConfig serverCfg = Seriex.get().fileManager().config(ServerConfig.class);
		MinigameConfig config = Seriex.get().fileManager().config(MinigameConfig.class);
		minigameMap.forEach((name, minigame) -> {
			String worldName = config.get(name, config.WORLD_NAME);
			if ("empty".equals(worldName)) {
				config.set(name, config.WORLD_NAME, serverCfg.WORLD_NAME.value());

				List<Double> spawnLocation = new ArrayList<>();
				for (int i = 0; i < 3; i++)
					spawnLocation.add(0.0);
				config.set(name, config.SPAWN_LOCATION, spawnLocation);

				List<List<Double>> bb = new ArrayList<>();
				for (int i = 0; i < 2; i++) {
					List<Double> axis = new ArrayList<>();
					for (int j = 0; j < 3; j++) {
						axis.add(0.0);
					}
					bb.add(axis);
				}
				config.set(name, config.ALLOWED_LIMIT, bb);
			} else {
				minigame.worldName = config.get(name, config.WORLD_NAME);
				Seriex.get().logger().debug("set worldName {} for {} ({}, {})",
							minigame.worldName,
							minigame.name,
							config.get(name, config.WORLD_NAME),
							name);
				List<Double> spawnLocationList = config.get(name, config.SPAWN_LOCATION);
				World world = Bukkit.getWorld(minigame.worldName);
				if (world == null) {
					Seriex.get().logger().error("World with the name '{}' is not found, available worlds: {}", minigame.worldName, Bukkit.getWorlds().stream().map(World::getName).toList());
					Seriex.get().logger().error("Trying to load world '{}'...", minigame.worldName);
					world = new WorldCreator(minigame.worldName)
								.environment(Environment.NORMAL)
								.generateStructures(false)
								.createWorld();
					if (world == null) {
						Seriex.get().logger().error("Couldnt load world '{}'.", minigame.worldName);
					}
				}
				minigame.spawnLocation = new Location(world, spawnLocationList.get(0), spawnLocationList.get(1), spawnLocationList.get(2));
				List<List<Double>> axisLimits = config.get(name, config.ALLOWED_LIMIT);
				List<Double> minimumLimits = axisLimits.get(0);
				List<Double> maximumLimits = axisLimits.get(1);
				Location minimum = new Location(world, minimumLimits.get(0), minimumLimits.get(1), minimumLimits.get(2));
				Location maximum = new Location(world, maximumLimits.get(0), maximumLimits.get(1), maximumLimits.get(2));
				minigame.allowedLimit = new AxisBB(minimum, maximum);
			}
		});
		super.post(seriex);
	}

	public Minigame[] minigames() {
		return minigameMap.values().toArray(Minigame[]::new);
	}
}
