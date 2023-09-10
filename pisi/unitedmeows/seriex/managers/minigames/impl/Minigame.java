package pisi.unitedmeows.seriex.managers.minigames.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.data.PlayerHistory;
import pisi.unitedmeows.seriex.managers.minigames.killstreak.IKillstreak;
import pisi.unitedmeows.seriex.managers.minigames.killstreak.impl.BasicStreak;
import pisi.unitedmeows.seriex.managers.minigames.killstreak.impl.PotPVPGappleStreak;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

// we dont use events here, we already have events in SeriexSpigotListener
public class Minigame {
	private static final BasicStreak KILLSTREAK = new BasicStreak();
	public String name, worldName;
	public Location spawnLocation;
	public AxisBB allowedLimit;
	public Set<UUID> playersInMinigame = new HashSet<>();
	private Map<UUID, PlayerHistory> playerHistory = new HashMap<>();
	private final Map<UUID, Integer> killStreakTracker = new HashMap<>();
	protected Runnable onJoinRunnable;

	public void onJoin(PlayerW playerW) {
		int delay = 0;

		Runnable joinActivities = () -> {
			UUID uniqueId = playerW.uuid();
			Player hooked = playerW.hook();
			playerHistory.put(uniqueId, PlayerHistory.createHistory(hooked));
			playerW.cleanupUser(true);
			World world = Bukkit.getWorld(worldName);
			spawnLocation.setWorld(world);
			world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
			hooked.setBedSpawnLocation(spawnLocation);
			hooked.teleport(spawnLocation);
			playersInMinigame.add(uniqueId);
			playerW.playerState(PlayerState.MINIGAMES);
			playerW.currentMinigame(this);
			onJoinRunnable.run();
		};

		Minigame currentMinigame = playerW.currentMinigame();
		if (currentMinigame != null && currentMinigame != this) {
			currentMinigame.onLeave(playerW);
			delay = 10;
		}

		if (delay == 0) joinActivities.run();
		else Seriex.get().runLater(joinActivities, delay);

	}


	public void onLeave(PlayerW playerW) {
		UUID uniqueId = playerW.uuid();
		Player hooked = playerW.hook();
		ServerConfig config = Seriex.get().fileManager().config(ServerConfig.class);
		World world = Bukkit.getWorld(config.WORLD_NAME.value());
		Location loc = config.getWorldSpawn();
		world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		hooked.setBedSpawnLocation(loc);
		hooked.teleport(loc);
		playersInMinigame.remove(uniqueId);
		playerW.playerState(PlayerState.SPAWN);
		playerW.currentMinigame(null);
		playerW.cleanupUser(true);
		playerHistory.get(uniqueId).restore(hooked);
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		if (!allowedLimit.intersectsWith(event.getTo())) {
			Player player = event.getPlayer();
			Seriex.get().msg(player, Messages.MINIGAME_OUT_OF_BOUNDS);
			player.setFallDistance(0.0F);
			player.teleport(spawnLocation);
		}
	}

	public void onPlayerDropItem(PlayerDropItemEvent event) {}

	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {}

	public void onHungerChange(FoodLevelChangeEvent event) {}

	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {}

	public void onPlayerDeath(PlayerDeathEvent event) {
		killStreakTracker.compute(event.getEntity().getUniqueId(), (uuid, kills) -> 0);
		Player player = event.getEntity();

		Player killer = event.getEntity().getKiller();
		AtomicBoolean unknownDeath = new AtomicBoolean(false);
		if (!isInGame(killer)) {
			unknownDeath.set(true);
		}
		Seriex.get().plugin().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
			if (unknownDeath.get()) {
				Seriex.get().msg(onlinePlayer, Messages.MINIGAME_DEATH, name, player.getName());
			} else Seriex.get().msg(onlinePlayer, Messages.MINIGAME_KILLED, name, player.getName(), killer.getName());
		});

		if (!unknownDeath.get()) {
			PlayerW user = Seriex.get().dataManager().user(killer);
			user.giveMoney(5);
		}
	}

	public Kit kit() {
		return null;
	}

	public void onRespawn(PlayerW playerW) {
		killStreakTracker.compute(playerW.uuid(), (uuid, kills) -> 0);
		playerW.cleanupUser(true);
	}

	public int streak(Player player) {
		return killStreakTracker.getOrDefault(player.getUniqueId(), 0);
	}

	public void calculateKillstreak(Player killer, KillstreakType type) {
		UUID uniqueId = killer.getUniqueId();
		Integer currentKills = killStreakTracker.compute(uniqueId, (UUID uuid, Integer kills) -> (kills == null ? 0 : kills) + 1);
		boolean gaveStreak = type.streak.giveStreak(this, killer, currentKills);
		if (gaveStreak) {
			Seriex.get().plugin().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
				Seriex.get().msg(onlinePlayer, Messages.MINIGAME_WINSTREAK, name, killer.getName(), currentKills);
			});
		}
	}

	public boolean isInGame(Player player) {
		return player != null && playersInMinigame.contains(player.getUniqueId());
	}

	public enum KillstreakType {
		BASIC(new BasicStreak()),
		POTPVP_GAPPLE(new PotPVPGappleStreak());

		private final IKillstreak streak;

		KillstreakType(IKillstreak streak) {
			this.streak = streak;
		}

		public IKillstreak streak() {
			return streak;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Minigame other = (Minigame) obj;
		return Objects.equals(name, other.name);
	}
}
