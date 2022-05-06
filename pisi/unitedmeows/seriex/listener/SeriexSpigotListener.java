package pisi.unitedmeows.seriex.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import pisi.unitedmeows.seriex.Seriex;

public class SeriexSpigotListener implements Listener {
	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		Seriex.get().logger().info("%s joined the server!", event.getPlayer().getName());
		Seriex.get().dataManager().addUser(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onLeave(final PlayerQuitEvent event) {
		Seriex.get().logger().info("%s left the server!", event.getPlayer().getName());
		Seriex.get().dataManager().removeUser(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onKick(final PlayerKickEvent event) {
		Seriex.get().logger().info("%s got kicked out of the server!", event.getPlayer().getName());
		Seriex.get().dataManager().removeUser(event.getPlayer());
	}

	@EventHandler
	public void onAsyncChat(final AsyncPlayerChatEvent event) {}
}
