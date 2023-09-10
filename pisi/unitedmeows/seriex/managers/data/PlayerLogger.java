package pisi.unitedmeows.seriex.managers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

public class PlayerLogger extends Manager {
	private Map<UUID, Set<UUID>> playersToLog;

	@Override
	public void start(Seriex seriex) {
		this.playersToLog = new HashMap<>();
	}

	public void logPlayers(PlayerMoveEvent event) {
		if (playersToLog.isEmpty())
			return;
		PlayerLog log = PlayerLog.createLog(event);
		boolean removeFromList = false;
		List<UUID> toRemove = null;
		for (Entry<UUID, Set<UUID>> entry : playersToLog.entrySet()) {
			UUID uuid = entry.getKey();
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				removeFromList = true;
				if (toRemove == null)
					toRemove = new ArrayList<>();
				toRemove.add(uuid);
				continue;
			}
			Set<UUID> value = entry.getValue();
			if (value.contains(log.player.getUniqueId())) {
				Seriex.get().msg_no_translation(player, "[PlayerLog] => %s", log.toString());
			}
		}

		if (removeFromList) {
			toRemove.forEach(playersToLog::remove);
		}
	}

	public void hookToPlayer(PlayerW logger, PlayerW logged) {
		UUID loggerUUID = logger.hook().getUniqueId();
		Set<UUID> value = playersToLog.get(loggerUUID);
		if (value == null)
			value = new HashSet<>();

		boolean added = value.add(logged.hook().getUniqueId());
		var playerName = logged.attribute(NAME);

		if (added) {
			Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is being logged!", playerName);
			playersToLog.put(loggerUUID, value);
		} else {
			Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is already being logged!", playerName);
		}
	}

	public void unhookToPlayer(PlayerW logger, PlayerW logged) {
		UUID loggerUUID = logger.hook().getUniqueId();
		var playerName = logged.attribute(NAME);

		Set<UUID> value = playersToLog.get(loggerUUID);
		if (value == null) {
			Seriex.get().msg_no_translation(logger.hook(), "You are not logging any players.", playerName);
			return;
		}
		boolean removed = value.remove(loggerUUID);
		if (removed) {
			Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is no longer being logged.", playerName);
			if (value.isEmpty()) {
				Seriex.get().msg_no_translation(logger.hook(), "No more players to log, removed from loggers.", playerName);
				playersToLog.remove(loggerUUID);
			} else
				playersToLog.put(loggerUUID, value);
		} else {
			Seriex.get().msg_no_translation(logger.hook(), "Player '%s' was not being logged.", playerName);
		}
	}

	public static class PlayerLog {
		private final Player player;
		private final Location from, to;

		public static PlayerLog createLog(PlayerMoveEvent event) {
			return new PlayerLog(event);
		}

		private PlayerLog(PlayerMoveEvent event) {
			this.player = event.getPlayer();
			this.from = event.getFrom();
			this.to = event.getTo();
		}

		public double hDistance() {
			double xDistance = to.getX() - from.getX();
			double zDistance = to.getZ() - from.getZ();

			return Math.sqrt(xDistance * xDistance + zDistance * zDistance);
		}

		public double vDistance() {
			return to.getY() - from.getY();
		}

		// [PlayerLog] => (ghost2173) [hDistance = 1, vDistance = 1]
		@Override
		public String toString() {
			return new StringBuilder()
						.append("(").append(player.getName()).append(") ")
						.append("[hDist = ").append(hDistance())
						.append(", vDist= ").append(vDistance())
						.append("]").toString();
		}
	}
}
