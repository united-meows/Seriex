package pisi.unitedmeows.seriex.listener;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;

public class SeriexSpigotListener implements Listener {
	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		Seriex.logger().info("%s joined the server!", event.getPlayer().getName());
		Seriex.get().dataManager().user(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAutoComplete(PlayerChatTabCompleteEvent event) {
		final Command cmd = Seriex.get().commandSystem().commandFromFull(event.getChatMessage());
		if (cmd != null) {
			// mfw autocomplete doesnt work thanks ersin
			cmd.executeAutoComplete(Seriex.get().dataManager().user(event.getPlayer()), event.getChatMessage(), event.getLastToken());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEvent interactEvent) {
		switch (interactEvent.getAction()) {
			case RIGHT_CLICK_BLOCK: {
				if (interactEvent.getClickedBlock() instanceof Sign) {
					Sign sign = (Sign) interactEvent.getClickedBlock();
					String line1 = ChatColor.stripColor(sign.getLine(0)).substring(1);
					line1 = line1.substring(0, line1.length() - 1);
					List<SignCommand> signCommands = Seriex.get().signManager().signCommands();
					for (int i = 0; i < signCommands.size(); i++) {
						SignCommand signCommand = signCommands.get(i);
						if (signCommand.trigger().equalsIgnoreCase(line1)) {
							signCommand.runRight(Seriex.get().dataManager().user(interactEvent.getPlayer()), sign);
							break;
						}
					}
				}
				break;
			}
			case LEFT_CLICK_BLOCK: {
				if (interactEvent.getClickedBlock() instanceof Sign) {
					Sign sign = (Sign) interactEvent.getClickedBlock();
					String line1 = ChatColor.stripColor(sign.getLine(0)).substring(1);
					line1 = line1.substring(0, line1.length() - 1);
					List<SignCommand> signCommands = Seriex.get().signManager().signCommands();
					for (int i = 0; i < signCommands.size(); i++) {
						SignCommand signCommand = signCommands.get(i);
						if (signCommand.trigger().equalsIgnoreCase(line1)) {
							signCommand.runLeft(Seriex.get().dataManager().user(interactEvent.getPlayer()), sign);
							break;
						}
					}
				}
				break;
			}
			default:
				break;
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
