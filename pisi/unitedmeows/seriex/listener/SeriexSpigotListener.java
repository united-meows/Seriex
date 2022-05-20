package pisi.unitedmeows.seriex.listener;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.sign.SignManager;
import pisi.unitedmeows.seriex.sign.SignCommand;

public class SeriexSpigotListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		Seriex.logger().info("%s joined the server!", event.getPlayer().getName());
		Seriex.get().dataManager().addUser(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEvent interactEvent) {
		switch (interactEvent.getAction()) {
			case RIGHT_CLICK_BLOCK: {

				if (interactEvent.getClickedBlock() != null) {
					if (interactEvent.getClickedBlock() instanceof Sign) {
						Sign sign = (Sign) interactEvent.getClickedBlock();
						String line1 = ChatColor.stripColor(sign.getLine(0)).
								substring(1);
						line1 = line1.substring(0, line1.length() - 1);
						for (SignCommand signCommand : Seriex.get().signManager().signCommands()) {
							if (signCommand.trigger().equalsIgnoreCase(line1)) {
								signCommand.runRight(Seriex.get().dataManager().addUser(interactEvent.getPlayer()));
								break;
							}

						}

					}
				}

				break;
			}
			case LEFT_CLICK_BLOCK: {
				if (interactEvent.getClickedBlock() != null) {
					if (interactEvent.getClickedBlock() instanceof Sign) {
						Sign sign = (Sign) interactEvent.getClickedBlock();
						String line1 = ChatColor.stripColor(sign.getLine(0)).
								substring(1);
						line1 = line1.substring(0, line1.length() - 1);
						for (SignCommand signCommand : Seriex.get().signManager().signCommands()) {
							if (signCommand.trigger().equalsIgnoreCase(line1)) {
								signCommand.runLeft(Seriex.get().dataManager().addUser(interactEvent.getPlayer()));
								break;
							}

						}

					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onLeave(final PlayerQuitEvent event) {
		Seriex.logger().info("%s left the server!", event.getPlayer().getName());
		Seriex.get().dataManager().removeUser(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onKick(final PlayerKickEvent event) {
		Seriex.logger().info("%s got kicked out of the server!", event.getPlayer().getName());
		Seriex.get().dataManager().removeUser(event.getPlayer());
	}

	@EventHandler
	public void onAsyncChat(final AsyncPlayerChatEvent event) {}
}
