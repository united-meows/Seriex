package pisi.unitedmeows.seriex.listener;

import static java.nio.charset.StandardCharsets.*;
import static java.time.Duration.*;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.*;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.util.config.impl.server.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.crasher.PlayerCrasher;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class SeriexSpigotListener implements Listener {
	private static final String OFFLINE_PLAYER = "OfflinePlayer:";

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		StructPlayer playerStruct = Seriex.get().database().getPlayer(name);
		Seriex.get().dataManager().user(player);
		if (playerStruct.banned) {
			Seriex.logger().info("%s is banned.", name);
			BanActionsConfig banActionsConfig = (BanActionsConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().BAN_ACTIONS);
			if (banActionsConfig.DISABLE_LOGIN.value()) {
				StringBuilder stringBuilder = new StringBuilder();
				Boolean trollLogin = banActionsConfig.LOGIN_TROLL.value();
				int times = 70; // TODO test
				if (trollLogin) {
					for (int i = 0; i < times; i++) {
						stringBuilder.append("\n");
					}
				}
				stringBuilder.append(Seriex.get().suffix());
				stringBuilder.append("\n");
				stringBuilder.append("&7You are &4&lbanned&r&7 from the server.");
				stringBuilder.append("\n");
				stringBuilder.append("&7Check your &cDM&7`s with &cSeriexBot or &c#ban-log&7");
				stringBuilder.append("\n");
				stringBuilder.append("&7to see the reason why you are banned.");
				if (trollLogin) {
					for (int i = 0; i < times; i++) {
						stringBuilder.append("\n");
					}
				}
				event.getPlayer().kickPlayer(Seriex.get().colorizeString(stringBuilder.toString()));
			}
			if (banActionsConfig.ANNOUNCE_IP_ON_JOIN.value()) {
				Seriex.get().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
					PlayerW hooked = Seriex.get().dataManager().user(onlinePlayer);
					// Player %s (IP: %s) tried to join the server, but is banned.
					// ^^ default message of vv
					// TODO ban_actions.ip_announcement <- add to config
					Seriex.get().sendMessage(player, Seriex.get().I18n().getString("ban_actions.ip_announcement", hooked), name, hooked.getMaskedIP(hooked.getIP()));
				});
			}
			if (banActionsConfig.DISABLE_DISCORD.value()) {
				JDA JDA = Seriex.get().discordBot().JDA();
				StructPlayerDiscord playerStructDiscord = Seriex.get().database().getPlayerDiscord(name);
				DiscordConfig config = (DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD);
				Guild guild = JDA.getGuildById(config.ID_GUILD.value());
				Member member = guild.getMember(UserSnowflake.fromId(playerStructDiscord.discord_id));
				// TODO should all bans be 7 days long?
				if (!member.isTimedOut()) {
					member.timeoutFor(ofDays(7));
				}
			}
			if (banActionsConfig.CRASH_GAME.value()) {
				PlayerCrasher.INSTANCE.fuck(player);
			}
			return;
		}
		Seriex.logger().info("%s joined the server!", name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) { // uuid spoof fix
		Player player = event.getPlayer();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(OFFLINE_PLAYER);
		stringBuilder.append(player.getName());
		UUID calculatedUUID = UUID.nameUUIDFromBytes(stringBuilder.toString().getBytes(UTF_8));
		if (!calculatedUUID.equals(player.getUniqueId())) {
			// TODO translations
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "spoofed uuid smh get real");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPreJoin(AsyncPlayerPreLoginEvent event) {
		String name = event.getName();
		if (name.length() < 3 && name.length() > 16) {
			event.disallow(KICK_WHITELIST, Seriex.get().colorizeString(String.format("%s%n&7Disallowed username.", Seriex.get().suffix())));
			return;
		}
		StructPlayer playerStruct = Seriex.get().database().getPlayer(name);
		if (playerStruct == null) {
			String discordLink = ((DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD)).INVITE_LINK.value();
			event.disallow(KICK_WHITELIST, String.format("%s%n&7Please register on the discord server. %n%s", Seriex.get().suffix(), discordLink));
			return;
		}
		Player player = Seriex.get().getServer().getPlayerExact(name);
		if (player != null) {
			// TODO translations
			event.disallow(KICK_WHITELIST, String.format("%s%n&7Player already online.", Seriex.get().suffix()));
		}
		// AntiBot :DDDDD
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAutoComplete(PlayerChatTabCompleteEvent event) {
		final Command cmd = Seriex.get().commandSystem().commandFromFull(event.getChatMessage());
		if (cmd != null) {
			cmd.executeAutoComplete(Seriex.get().dataManager().user(event.getPlayer()), event.getChatMessage(), event.getLastToken());
		}
		// todo add suggester when settings-ui has language
		//		event.getTabCompletions().addAll(suggester.autocomplete(event.getLastToken(), 5));
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
		Player player = event.getPlayer();
		Seriex.logger().info("%s left the server!", player.getName());
		Seriex.get().dataManager().removeUser(player);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onKick(final PlayerKickEvent event) {
		Player player = event.getPlayer();
		Seriex.logger().info("%s got kicked out of the server!", player.getName());
		Seriex.get().dataManager().removeUser(player);
	}

	@EventHandler
	public void onAsyncChat(final AsyncPlayerChatEvent event) {}
}
