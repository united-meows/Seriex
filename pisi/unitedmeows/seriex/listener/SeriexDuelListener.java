package pisi.unitedmeows.seriex.listener;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.api.event.request.RequestAcceptEvent;
import me.realized.duels.api.event.request.RequestSendEvent;
import me.realized.duels.api.event.spectate.SpectateStartEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;

public class SeriexDuelListener implements Listener {

	@EventHandler
	public void onDuelRequestAccept(RequestAcceptEvent event) {
		var source = event.getSource();
		if (!isPlayerAvailable(source)) {
			Seriex.get().msg(source, Messages.DUEL_CANT_ACCEPT);
			event.setCancelled(true);
			return;
		}
		var target = event.getTarget();
		if (!isPlayerAvailable(target)) {
			Seriex.get().msg(target, Messages.DUEL_CANT_ACCEPT);
			Seriex.get().msg(source, Messages.DUEL_CANT_ACCEPT_OTHER, target.getName());
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDuelRequestSend(RequestSendEvent event) {
		var source = event.getSource();
		if (!isPlayerAvailable(source)) {
			Seriex.get().msg(source, Messages.DUEL_CANT_SEND);
			event.setCancelled(true);
			return;
		}
		var target = event.getTarget();
		if (!isPlayerAvailable(target)) {
			Seriex.get().msg(target, Messages.DUEL_CANT_SEND);
			Seriex.get().msg(source, Messages.DUEL_CANT_SEND_OTHER, target.getName());
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDuelSpectate(SpectateStartEvent event) {
		var source = event.getSource();
		if (!isPlayerAvailable(source)) {
			Seriex.get().msg(source, Messages.DUEL_CANT_SPECTATE);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDuelStart(MatchStartEvent event) {
		for (Player player : event.getPlayers()) {
			var user = Seriex.get().dataManager().user(player);
			user.playerState(PlayerState.DUEL);
		}
	}

	@EventHandler
	public void onDuelEnd(MatchEndEvent event) {
		endDuel(event.getWinner(), event.getLoser());
	}

	public void endDuel(UUID... playerUUIDs) {
		Arrays.stream(playerUUIDs)
					.map(Bukkit::getPlayer)
					.filter(Objects::nonNull)
					.map(Seriex.get().dataManager()::user)
					.forEach(user -> user.playerState(PlayerState.SPAWN));
	}

	public boolean isPlayerAvailable(Player player) {
		var user = Seriex.get().dataManager().user(player);
		return user.playerState() == PlayerState.SPAWN;
	}
}
