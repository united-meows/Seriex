package pisi.unitedmeows.seriex.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.player.PlayerW;

public class SeriexRawListener implements Listener {


	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent event) {
		Seriex.playerw(event.getPlayer()).onJoin();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onLeave(PlayerQuitEvent event) {
		PlayerW playerW = Seriex.removePlayerW(event.getPlayer());
		if (playerW != null)
			playerW.onLeave();
	}
}
