package pisi.unitedmeows.seriex.managers.data;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

import java.util.List;

import static pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes.NAME;

public class PlayerLogger extends Manager {
    private List<PlayerW> loggers;

	@Override
	public void start(Seriex seriex) {
		this.loggers = Lists.newArrayList();
	}

	public void logPlayers(PlayerMoveEvent event) {
		if (loggers.isEmpty())
			return;
		PlayerLog log = PlayerLog.createLog(event);
        for (int i = 0; i < loggers.size(); i++) {
            PlayerW logger = loggers.get(i);
            Player player = Bukkit.getPlayer(logger.uuid());
            if (player == null) {
                loggers.remove(i--);
                continue;
            }
            if (logger.lookupMap().containsKey(log.player.getUniqueId())) {
                Seriex.get().msg_no_translation(player, "[PlayerLog] => %s", log.toString());
            }
        }
	}

	public void hookToPlayer(PlayerW logger, PlayerW logged) {
        var playerName = logged.attribute(NAME);
        if (logger.lookupMap().containsKey(logged.uuid())) {
            Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is already being logged!", playerName);
        } else {
            logger.lookupMap().put(logged.uuid(), logged);
            if (!loggers.contains(logger)) {
                loggers.add(logger);
            }
            Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is being logged!", playerName);
        }
	}

	public void unhookToPlayer(PlayerW logger, PlayerW logged) {
		var playerName = logged.attribute(NAME);
		if (logger.lookupMap().isEmpty()) {
			Seriex.get().msg_no_translation(logger.hook(), "You are not logging any players.", playerName);
			return;
		}
        boolean removed = logger.lookupMap().remove(logged.uuid()) != null;
		if (removed) {
            if (logger.lookupMap().isEmpty()) {
                loggers.remove(logger);
            }
			Seriex.get().msg_no_translation(logger.hook(), "Player '%s' is no longer being logged.", playerName);
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
