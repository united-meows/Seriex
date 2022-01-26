package pisi.unitedmeows.seriex.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.player.PlayerW;

public class SeriexRawListener implements Listener {
	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) { Seriex.playerw(event.getPlayer()).onJoin(); }

	@EventHandler(priority = EventPriority.LOW)
	public void onLeave(final PlayerQuitEvent event) {
		final PlayerW playerW = Seriex.removePlayerW(event.getPlayer());
		if (playerW != null) {
			playerW.onLeave();
		}
	}

	@EventHandler
	public void onAsyncChat(final AsyncPlayerChatEvent event) {
		if (event.getMessage().startsWith(Seriex._self.commandSystem().prefix())) {
			event.setCancelled(true);
			Seriex._self.commandSystem().execute(Seriex.playerw(event.getPlayer()),
						event.getMessage());
		}
	}
}
