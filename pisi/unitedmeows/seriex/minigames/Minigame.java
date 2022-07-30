package pisi.unitedmeows.seriex.minigames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.minigames.killstreak.BasicKillstreak;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class Minigame implements Listener {
	private static final BasicKillstreak KILLSTREAK = new BasicKillstreak();
	public String name , worldName;
	public Location spawnLocation;
	public AxisBB allowedLimit;
	public List<PlayerW> playersInMinigame = new GlueList<>();
	private HashMap<PlayerW, PlayerInventory> lastInventories = new HashMap<>();
	private final Map<UUID, Integer> killStreakTracker = new HashMap<>();

	public void onJoin(PlayerW playerW) {
		lastInventories.put(playerW, playerW.getHooked().getInventory());
		playerW.cleanupUser(true);
		World world = Bukkit.getWorld(worldName);
		spawnLocation.setWorld(world);
		world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
		playerW.getHooked().setBedSpawnLocation(spawnLocation);
		playersInMinigame.add(playerW);
		playerW.currentMinigame = this;
	}

	//TODO: onGuiSwitch pls use onLeave for old minigame (if present) then use onJoin for clicked minigame
	//TODO: on /spawn pls use onLeave playerW.currentMinigame (if present)
	public void onLeave(PlayerW playerW) {
		playerW.getHooked().getInventory().setContents(lastInventories.get(playerW).getContents());
		lastInventories.remove(playerW);
		World world = Bukkit.getWorld(((ServerConfig) Seriex.get().fileManager().getConfig(FileManager.SERVER)).WORLD_NAME.value());
		Location loc = ((ServerConfig) Seriex.get().fileManager().getConfig(FileManager.SERVER)).getWorldSpawn();
		world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		playerW.getHooked().setBedSpawnLocation(loc);
		playersInMinigame.remove(playerW);
		playerW.currentMinigame = null;
	}

	public void calculateKillstreak(Player killer, ItemStack... itemsToGive) {
		UUID uniqueId = killer.getUniqueId();
		killStreakTracker.compute(uniqueId, (UUID uuid, Integer kills) -> (kills == null ? 0 : kills) + 1);
		KILLSTREAK.giveStreak(killer, killStreakTracker.get(uniqueId), itemsToGive);
	}

	public boolean isInGame(Player player) {
		return player != null && playersInMinigame.contains(Seriex.get().dataManager().user(player));
	}
}
