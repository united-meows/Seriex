package pisi.unitedmeows.seriex.auth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class AuthListener implements Listener {
	private Map<PlayerW, Long> playerMap = new HashMap<>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {}
}
