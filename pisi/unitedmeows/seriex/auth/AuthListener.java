package pisi.unitedmeows.seriex.auth;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import pisi.unitedmeows.pispigot.Pispigot;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.impl.server.AuthConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.clazz.HookClass;

// TODO finish
public class AuthListener extends Manager implements org.bukkit.event.Listener {
	private Map<PlayerW, AuthInfo> playerMap = new HashMap<>();

	@Override
	public void start(Seriex seriex) {
		Bukkit.getPluginManager().registerEvents(this, Seriex.get());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final PlayerW playerW = Seriex.get().dataManager().user(event.getPlayer());
		final AuthInfo authentication = new AuthInfo(playerW);
		playerMap.put(playerW, authentication);
		authentication.onJoin();
		Pispigot.playerSystem(event.getPlayer()).subscribeAll(this);
	}

	@Override
	public void cleanup() throws SeriexException {
		playerMap.forEach((PlayerW k, AuthInfo v) -> v.onServerEnd());
		playerMap.clear();
	}

	public void stopAuthentication(PlayerW playerW) {
		Pispigot.playerSystem(playerW.getHooked()).unsubscribeAll(this);
		final AuthInfo authentication = playerMap.remove(playerW);
		authentication.onLogin();
	}

	public class AuthInfo extends HookClass<PlayerW> {
		public final Location spawnLocation = getServerConfig().getWorldSpawn();
		private long startMS = System.currentTimeMillis() , endMS;
		private AuthState state = AuthState.WAITING;
		private long benchmark;

		public void onJoin() {}

		public void onLogin() {}

		public void onServerEnd() {}

		public void onAuthInterrupted() {}

		public AuthInfo(PlayerW player) {
			this.hooked = player;
		}

		@Override
		protected PlayerW getHooked() {
			benchmark++;
			return super.getHooked();
		}
	}

	public enum AuthState {
		WAITING,
		LOGGED_IN
	}

	public AuthInfo getAuthInfo(Player player) {
		return playerMap.get(Seriex.get().dataManager().user(player));
	}

	public boolean waitingForLogin(Player player) {
		return getAuthInfo(player).state == AuthState.WAITING;
	}

	private TranslationsConfig getTranslationConfig() {
		return (TranslationsConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().TRANSLATIONS);
	}

	private AuthConfig getAuthConfig() {
		return (AuthConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().AUTH);
	}

	private ServerConfig getServerConfig() {
		return (ServerConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().SERVER);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player)) {
			String cmd = event.getMessage().split(" ")[0].toLowerCase();
			AuthConfig authConfig = getAuthConfig();
			if (!authConfig.ALLOWED_COMMANDS.value().contains(cmd)) return;
			event.setCancelled(true);
			TranslationsConfig translationConfig = getTranslationConfig();
			// TODO set ("auth.command_not_allowed") in TranslationConfig
			// The command %s is not allowed! <- default message
			String value = translationConfig.getValue("auth.command_not_allowed", translationConfig.config);
			Seriex.get().sendMessage(player, value);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player)) {
			event.setCancelled(true);
			TranslationsConfig translationConfig = (TranslationsConfig) Seriex.get().fileManager().getConfig(Seriex.get().fileManager().TRANSLATIONS);
			// TODO set ("auth.chat_not_allowed") in TranslationConfig
			// In order to chat you must be authenticated! <- default message
			String value = translationConfig.getValue("auth.chat_not_allowed", translationConfig.config);
			Seriex.get().sendMessage(player, value);
		} else {
			event.getRecipients().removeIf(this::waitingForLogin);
			if (event.getRecipients().isEmpty()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!waitingForLogin(player)) return;
		Location from = event.getFrom();
		Location to = event.getTo();
		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getY() - to.getY() >= 0.0D) return;
		if (getAuthInfo(player).spawnLocation.distance(from) > getAuthConfig().ALLOWED_MOVEMENT_DELTA.value()) {
			event.setTo(event.getFrom());
		}
	}
}
