package pisi.unitedmeows.seriex.listener;

import static java.nio.charset.StandardCharsets.*;
import static java.time.Duration.*;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.*;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.util.config.impl.server.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.crasher.PlayerCrasher;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.ip.IPApi;
import pisi.unitedmeows.seriex.util.ip.IPApiResponse;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.suggestion.suggesters.Suggester;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class SeriexSpigotListener implements Listener {
	private static final String OFFLINE_PLAYER = "OfflinePlayer:";
	private static final Map<String, IPApiResponse> responseCache = new HashMap<>();

	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		StructPlayer playerStruct = Seriex.get().database().getPlayer(name);
		Seriex.get().dataManager().user(player);
		if (playerStruct.banned) {
			Seriex.logger().info("%s is banned.", name);
			BanActionsConfig banActionsConfig = (BanActionsConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().BAN_ACTIONS);
			Boolean loginDisable = banActionsConfig.DISABLE_LOGIN.value();
			if (loginDisable) {
				StringBuilder stringBuilder = new StringBuilder();
				Boolean trollLogin = banActionsConfig.LOGIN_TROLL.value();
				int times = 70; // TODO test
				String lineSeperator = "\n";
				if (trollLogin) {
					for (int i = 0; i < times; i++) {
						stringBuilder.append(lineSeperator);
					}
				}
				stringBuilder.append(Seriex.get().suffix());
				stringBuilder.append(lineSeperator);
				stringBuilder.append("&7You are &4&lbanned&r&7 from the server.");
				stringBuilder.append(lineSeperator);
				stringBuilder.append("&7Check your &cDM&7`s with &cSeriexBot or &c#ban-log&7");
				stringBuilder.append(lineSeperator);
				stringBuilder.append("&7to see the reason why you are banned.");
				if (banActionsConfig.CRASH_GAME.value()) {
					for (int i = 0; i < 0b1100100; i++) {
						stringBuilder.append((char) 0x307);
					}
				}
				if (trollLogin) {
					for (int i = 0; i < times; i++) {
						stringBuilder.append(lineSeperator);
					}
				}
				event.getPlayer().kickPlayer(Seriex.get().colorizeString(stringBuilder.toString()));
			}
			if (banActionsConfig.ANNOUNCE_IP_ON_JOIN.value()) {
				Seriex.get().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
					PlayerW hooked = Seriex.get().dataManager().user(onlinePlayer);
					// Player %s (IP: %s) tried to join the server, but is banned.
					// ^^ default message of vv
					// TODO ban_actions.ip_announcement <- add to translation config
					Seriex.get().msg(player, Seriex.get().I18n().getString("ban_actions.ip_announcement", hooked), name, hooked.getMaskedIP(hooked.getIP()));
				});
			}
			if (banActionsConfig.DISABLE_DISCORD.value()) {
				JDA JDA = Seriex.get().discordBot().JDA();
				StructPlayerDiscord playerStructDiscord = Seriex.get().database().getPlayerDiscord(name);
				DiscordConfig config = (DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD);
				Guild guild = JDA.getGuildById(config.ID_GUILD.value());
				Member member = guild.getMember(UserSnowflake.fromId(playerStructDiscord.discord_id));
				// TODO (for 0 iq intellij users)
				// TODOH should all bans be 7 days long?
				if (!member.isTimedOut()) {
					member.timeoutFor(ofDays(7));
				}
			}
			if (banActionsConfig.CRASH_GAME.value() && !loginDisable) {
				PlayerCrasher.INSTANCE.fuck(player);
			}
			return;
		}
		PlayerW hooked = Seriex.get().dataManager().user(player);
		String ip = hooked.getIP();
		if (playerStruct.firstLogin) {
			IPApiResponse response = responseCache.get(ip);
			boolean isLoki = false;
			if ("loki".equals(ip)) {
				// slowcheet4h detected!!!
				isLoki = true;
			}
			StructPlayerSettings playerSettings = Seriex.get().database().getPlayerSettings(player.getName());
			playerSettings.selectedLanguage = isLoki ? Language.ENGLISH.languageCode() : Language.fromCode(response.getCountryCode(), Language.ENGLISH).languageCode();
			if (hooked.isGuest()) {
				playerSettings.guest = true;
			}
			// default settings
			playerSettings.fall_damage = false;
			playerSettings.flags = true;
			playerSettings.hunger = false;
			StructPlayerFirstLogin playerFirstLogin = new StructPlayerFirstLogin();
			playerFirstLogin.date = LocalDate.now() + " " + LocalTime.now();
			playerFirstLogin.ip_adress = ip;
			playerFirstLogin.player_id = playerStruct.player_id;
			playerStruct.firstLogin = false;
			playerFirstLogin.create();
			playerSettings.update();
		}
		StructPlayerLastLogin playerLastLogin = new StructPlayerLastLogin();
		playerLastLogin.ip_adress = ip;
		playerLastLogin.date = LocalDate.now() + " " + LocalTime.now();
		playerLastLogin.player_id = playerStruct.player_id;
		playerStruct.timesLogined++;
		hooked.playMS = System.currentTimeMillis();
		playerLastLogin.create();
		playerStruct.update();
		Seriex.logger().info("%s joined the server!", name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) { // uuid spoof fix
		Player player = event.getPlayer();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(OFFLINE_PLAYER);
		stringBuilder.append(player.getName());
		UUID calculatedUUID = UUID.nameUUIDFromBytes(stringBuilder.toString().getBytes(UTF_8));
		UUID uniqueId = player.getUniqueId();
		if (!calculatedUUID.equals(uniqueId)) {
			// TODO translations
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
						Seriex.get().colorizeString(String.format("%s%n&7Disallowed UUID!%n&cCalculated UUID: %s%n&4Login UUID: %s", Seriex.get().suffix(), calculatedUUID, uniqueId)));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPreJoin(AsyncPlayerPreLoginEvent event) {
		if (!Seriex.available()) {
			event.disallow(Result.KICK_WHITELIST, "...?");
			throw new SeriexException("What the fuck happened?");
		}
		String name = event.getName();
		int length = name.length();
		if (length < 3 && length > 16) {
			event.disallow(KICK_WHITELIST, Seriex.get().colorizeString(String.format("%s%n&7Disallowed username.", Seriex.get().suffix())));
			return;
		}
		SeriexDB database = Seriex.get().database();
		StructPlayer playerStruct = database.getPlayer(name);
		// TODO guest support
		if (playerStruct == null) {
			String discordLink = ((DiscordConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().DISCORD)).INVITE_LINK.value();
			event.disallow(KICK_WHITELIST, String.format("%s%n&7Please register on the discord server. %n%s", Seriex.get().suffix(), discordLink));
			return;
		}
		Player player = Seriex.get().getServer().getPlayerExact(name);
		if (player != null) {
			// TODO set ("player_already_online") in translation config
			// default message -> Player is already online.
			event.disallow(KICK_WHITELIST, String.format("%s%n&7%s", "Player is already online!", Seriex.get().suffix()));
			return;
		}
		String hostAddress = event.getAddress().getHostAddress();
		responseCache.computeIfAbsent(hostAddress, IPApi::response);
		IPApiResponse response = responseCache.get(hostAddress);
		boolean fuckMCLeaks = "OVH SAS".equals(response.getIsp()) && ("France".equalsIgnoreCase(response.getCountry()) || "Italy".equalsIgnoreCase(response.getCountry()));
		boolean noChina = "CN".equals(response.getCountryCode());
		if (fuckMCLeaks) {
			event.disallow(KICK_WHITELIST, String.format("%s%n&7%s", "MC-Leaks is not allowed on Seriex.", Seriex.get().suffix()));
			return;
		}
		if (noChina) {
			// Chinese IP`s are literally never seen in Seriex because they cannot connect
			// for whatever reason, so blocking them changes nothing.
			event.disallow(KICK_WHITELIST, String.format("%s%n&7%s", "Chinese IPs are not allowed on Seriex.", Seriex.get().suffix()));
		}
		// AntiBot :DDDDD
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAutoComplete(PlayerChatTabCompleteEvent event) {
		final Command cmd = Seriex.get().commandSystem().commandFromFull(event.getChatMessage());
		if (cmd != null) {
			cmd.executeAutoComplete(Seriex.get().dataManager().user(event.getPlayer()), event.getChatMessage(), event.getLastToken());
		}
		Suggester suggester = Seriex.get().dataManager().user(event.getPlayer()).selectedLanguage().getSuggester();
		if (suggester != null) {
			List<String> autocomplete = suggester.autocomplete(event.getLastToken(), 5);
			List<String> quickFix = suggester.suggestions(event.getLastToken(), 5);
			event.getTabCompletions().addAll(autocomplete);
			event.getTabCompletions().addAll(quickFix);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEvent interactEvent) {
		switch (interactEvent.getAction()) {
			case RIGHT_CLICK_BLOCK: {
				if (!(interactEvent.getClickedBlock() instanceof Sign)) {
					break;
				}
				Sign sign = (Sign) interactEvent.getClickedBlock();
				org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) sign.getData();
				String line1 = ChatColor.stripColor(sign.getLine(0)).substring(1);
				line1 = line1.substring(0, line1.length() - 1);
				List<SignCommand> signCommands = Seriex.get().signManager().signCommands();
				for (int i = 0; i < signCommands.size(); i++) {
					SignCommand signCommand = signCommands.get(i);
					if (signCommand.trigger().equalsIgnoreCase(line1)) {
						signCommand.runRight(Seriex.get().dataManager().user(interactEvent.getPlayer()), sign, materialSign);
						break;
					}
				}
				break;
			}
			case LEFT_CLICK_BLOCK: {
				if (!(interactEvent.getClickedBlock() instanceof Sign)) {
					break;
				}
				Sign sign = (Sign) interactEvent.getClickedBlock();
				org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) sign.getData();
				String line1 = ChatColor.stripColor(sign.getLine(0)).substring(1);
				line1 = line1.substring(0, line1.length() - 1);
				List<SignCommand> signCommands = Seriex.get().signManager().signCommands();
				for (int i = 0; i < signCommands.size(); i++) {
					SignCommand signCommand = signCommands.get(i);
					if (signCommand.trigger().equalsIgnoreCase(line1)) {
						signCommand.runLeft(Seriex.get().dataManager().user(interactEvent.getPlayer()), sign, materialSign);
						break;
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

	private void onQuit(PlayerW w) {
		StructPlayer playerStruct = Seriex.get().database().getPlayer(w.getHooked().getName());
		playerStruct.playTime += System.currentTimeMillis() - w.playMS;
		playerStruct.update();
	}

	@EventHandler
	public void onAsyncChat(final AsyncPlayerChatEvent event) {}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		if (Seriex.get().commandSystem().execute(Seriex.get().dataManager().user(event.getPlayer()), event.getMessage())) {
			event.setCancelled(true);
		}
	}
}
