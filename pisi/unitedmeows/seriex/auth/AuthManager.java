package pisi.unitedmeows.seriex.auth;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.auth.gauth.GAuth;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.multi.impl.TranslationsConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.AuthConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.title.AnimatedTitle;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.clazz.HookClass;

// TODO finish
public class AuthManager extends Manager implements Listener {
	private Cache<UUID, String> loggedInIPCache;
	private Map<PlayerW, AuthInfo> authInfoMap;
	private boolean shooterIsLivingEntity;
	private Method getShooter;
	private Map<UUID, String> MFA;

	@Override
	public void start(Seriex seriex) {
		try {
			this.getShooter = Projectile.class.getDeclaredMethod("getShooter");
			this.shooterIsLivingEntity = this.getShooter.getReturnType() == LivingEntity.class;
		}
		catch (NoSuchMethodException
					| SecurityException e) {
			seriex.logger().error("Cannot load getShooter() method on Projectile class", e);
		}

		this.loggedInIPCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterWrite(Duration.ofMinutes(30L)).build();
		this.authInfoMap = new HashMap<>();
		this.MFA = new HashMap<>();
	}

	@EventHandler
	public void onPreJoin(AsyncPlayerPreLoginEvent event) {}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoinLowest(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!canApplyProtection(player)) {
			computeAuthInfo(player);
			PlayerW user = Seriex.get().dataManager().user(player);
			player.teleport(getServerConfig().getWorldSpawn());
			user.playerState(PlayerState.SPAWN);
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		authInfoMap.forEach((PlayerW player, AuthInfo info) -> info.onServerEnd());
		authInfoMap.clear();
	}

	public void stopAuthentication(AuthInfo info) {
		info.onLogin(info.getHooked().has2FA());
	}

	public void stopAuthentication(PlayerW playerW) {
		stopAuthentication(authInfoMap.get(playerW));
	}

	public class AuthInfo extends HookClass<PlayerW> {
		public final Location spawnLocation = getServerConfig().getWorldSpawn();
		private AuthState state = AuthState.WAITING;
		private BukkitRunnable joinMessageRunnable;
		private String lastCode;

		public void onJoin(AuthInfo info) {
			PlayerW baseHook = info.getHooked();
			Player player = baseHook.hook();
			ServerConfig serverConfig = getServerConfig();
			AuthConfig authConfig = getAuthConfig();
			String serverName = serverConfig.SERVER_NAME.value();
			String[] welcomeAnimated = AnimatedTitle.animateText(
						Seriex.get().I18n().getMessage(Messages.SERVER_WELCOME, baseHook, serverConfig.SERVER_NAME.value()), serverName, "&d", "&5&l");
			info.joinMessageRunnable = AnimatedTitle.animatedTitle(player, welcomeAnimated, null);
			var playerLogins = baseHook.playerLogins();
			var playerLastLogin = !playerLogins.isEmpty() ? playerLogins.get(playerLogins.size() - 1) : null;
			String databaseIP = playerLastLogin == null ? null : playerLastLogin.ip_address;
			if (playerLastLogin == null) {
				Seriex.get().logger().error("Player '{}' does not have a row in the table player_login!", player.getName());
			}
			String cachedIP = loggedInIPCache.getIfPresent(baseHook.uuid());
			if (authConfig.SESSION.value()
						&& databaseIP != null && cachedIP != null
						&& cachedIP.equals(baseHook.getIP())
						&& databaseIP.equals(baseHook.getIP())) {
				stopAuthentication(info);
				return;
			}
			Seriex.get().runLater(baseHook::denyMovement, 1);
			Seriex.get().inventoryPacketAdapter().sendBlankInventoryPacket(player);
			Seriex.get().msg(player, Messages.AUTH_TYPE_LOGIN_CMD);
			// LoginGUI.open(baseHook, Seriex.get().authentication());
			// TODO guest support
		}


		private void handle2FA(PlayerW playerW) {
			UUID uuid = playerW.uuid();
			if(MFA.get(uuid) != null)
				MFA.remove(uuid);

			String randomCode = String.valueOf(RandomUtils.nextInt(1000, 10000));
			MFA.put(uuid, randomCode);
			Seriex.get().discordBot().sendDirectMessage(getHooked().playerDiscord().snowflake, "Your 2FA code is: ||%s||".formatted(randomCode));
			Seriex.get().msg(getHooked().hook(), Messages.AUTH_2FA_SENT);
		}

		public void onLogin(boolean _2FA) {
			if (_2FA && state == AuthState.WAITING) {
				handle2FA(getHooked());
				state = AuthState.MFA;
				Seriex.get().msg(getHooked().hook(), Messages.AUTH_TYPE_2FA);
				return;
			}
			PlayerW baseHook = getHooked();
			baseHook.loggedIn(true);
			state = AuthState.LOGGED_IN;
			remove();
			Seriex.get().msg(baseHook.hook(), Messages.AUTH_LOGGED_IN);
			loggedInIPCache.put(
						baseHook.uuid(),
						baseHook.getIP());
			Seriex.get().runLater(() -> {
				baseHook.allowMovement();
				baseHook.hook().updateInventory();
			}, 1);
		}

		public void onServerEnd() {}

		public void onAuthInterrupted() {
			remove();
		}

		private void remove() {
			PlayerW hook = getHooked();
			authInfoMap.remove(hook);
		}

		public AuthInfo(PlayerW player) {
			this.hooked = player;
		}

		@Override
		public PlayerW getHooked() { return super.getHooked(); }
	}

	public enum AuthState {
		WAITING,
		MFA, // multi factor auth
		LOGGED_IN
	}

	public AuthInfo getAuthInfo(Player player) {
		return getAuthInfo(player, true);
	}

	public AuthInfo getAuthInfo(Player player, boolean computeIfAbsent) {
		try {
			PlayerW playerW = Seriex.get().dataManager().user(player, computeIfAbsent);
			if (playerW == null) { return null; }
			return authInfoMap.get(playerW);
		}
		catch (SeriexException e) {
			return null;
		}
	}

	public void computeAuthInfo(Player player) {
		authInfoMap.computeIfAbsent(Seriex.get().dataManager().user(player), computedPlayer -> {
			AuthInfo authInfo = new AuthInfo(computedPlayer);
			authInfo.onJoin(authInfo);
			return authInfo;
		});
	}

	public boolean canApplyProtection(Player player) {
		return getAuthInfo(player) != null;
	}

	public boolean waitingForLogin(Player player) {
		if (!canApplyProtection(player))
			return false;

		AuthInfo authInfo = getAuthInfo(player);
		return authInfo.state == AuthState.WAITING || authInfo.state == AuthState.MFA;
	}


	private AuthConfig getAuthConfig() { return Seriex.get().fileManager().config(AuthConfig.class); }

	private ServerConfig getServerConfig() { return Seriex.get().fileManager().config(ServerConfig.class); }

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player) && !event.getMessage().contains("/login")) {
			event.setCancelled(true);
			Seriex.get().msg(player, Messages.AUTH_COMMAND_NOT_ALLOWED, event.getMessage());
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player)) {
			AuthInfo authInfo = getAuthInfo(player);
			PlayerW hook = Seriex.get().dataManager().user(player);
			event.setCancelled(true);
			if (canApplyProtection(player) && authInfo.state == AuthState.MFA) {
				if (event.getMessage().equals(MFA.get(player.getUniqueId()))) {
					stopAuthentication(hook);
				} else {
					Seriex.get().msg(player, Messages.AUTH_INCORRECT_2FA);
				}
				return;
			}
			Seriex.get().msg(player, Messages.AUTH_CHAT_NOT_ALLOWED);
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

		if (from.getBlockX() == to.getBlockX()
					&& from.getBlockZ() == to.getBlockZ()
					&& from.getY() - to.getY() >= 0.0D)
			return;

		Location spawnLocation = getAuthInfo(player).spawnLocation;
		if (spawnLocation.distance(from) > getAuthConfig().ALLOWED_MOVEMENT_DELTA.value()) {
			player.teleport(spawnLocation);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!waitingForLogin(event.getPlayer()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!waitingForLogin(event.getPlayer()))
			return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if (entityEvent_preCheck(event)) {
			event.getEntity().setFireTicks(0);
			event.setDamage(0.0D);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onAttack(EntityDamageByEntityEvent event) {
		if (entityEvent_preCheck(event)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (entityEvent_preCheck(event)) {
			event.setTarget(null);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (entityEvent_preCheck(event))
			return;

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void entityRegainHealthEvent(EntityRegainHealthEvent event) {
		if (entityEvent_preCheck(event)) {
			event.setAmount(0.0D);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.HIGHEST)
	public void onEntityInteract(EntityInteractEvent event) {
		if (entityEvent_preCheck(event))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onLowestEntityInteract(EntityInteractEvent event) {
		if (entityEvent_preCheck(event))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() == null)
			return;
		Projectile projectile = event.getEntity();
		Object shooterRaw = null;
		if (this.shooterIsLivingEntity) {
			try {
				if (this.getShooter == null) {
					this.getShooter = Projectile.class.getMethod("getShooter");
				}
				// hopefully we throw an exception if getShooter is null
				// & invoke doesnt happen so it doesnt fuck tps
				shooterRaw = this.getShooter.invoke(projectile);
			}
			catch (Exception e) {
				Seriex.get().logger().error("Error getting shooter {}", e.getMessage());
			}
		} else {
			shooterRaw = projectile.getShooter();
		}
		if (shooterRaw instanceof Player playerShooter && this.waitingForLogin(playerShooter)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoinHigh(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		Seriex.get().plugin().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
			Seriex.get().msg(onlinePlayer, Messages.AUTO_JOIN_MESSAGE, event.getPlayer().getName());
		});
		Seriex.get().runLater(() -> {
			Location worldSpawn = getServerConfig().getWorldSpawn();
			if (event.getPlayer() != null && event.getPlayer().isOnline() && worldSpawn != null) {
				event.getPlayer().teleport(worldSpawn);
			}
		}, 3);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AuthInfo authInfo = getAuthInfo(player, false);
		if (authInfo == null || !waitingForLogin(player))
			return;
		event.setQuitMessage(null);
		authInfo.onAuthInterrupted();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		AuthInfo authInfo = getAuthInfo(player, false);
		if (authInfo == null || !waitingForLogin(player))
			return;

		authInfo.onAuthInterrupted();
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerHeldItem(PlayerItemHeldEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInventoryOpen(InventoryOpenEvent event) {
		HumanEntity player = event.getPlayer();
		if (player instanceof Player entityPlayer) {
			if (waitingForLogin(entityPlayer)) {
				event.setCancelled(true);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Seriex.get().plugin(), entityPlayer::closeInventory, 1L);
			}
		} else {
			Seriex.get().logger().error("göte geldik HumanEntity Playerin Master Classi olmuyomuş yardım edin");
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player entityPlayer) {
			if (waitingForLogin(entityPlayer)) {
				event.setCancelled(true);
			}
		} else {
			Seriex.get().logger().error("HumanEntity is not instanceof Player... @ghost pls fix.");
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerHitPlayerEvent(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player entityPlayer && waitingForLogin(entityPlayer)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setRespawnLocation(getServerConfig().getWorldSpawn());
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerShear(PlayerShearEntityEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerFish(PlayerFishEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if (waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.NORMAL)
	public void onShoot(EntityShootBowEvent event) {
		if (entityEvent_preCheck(event))
			event.setCancelled(true);
	}

	private boolean entityEvent_preCheck(EntityEvent event) {
		return event.getEntity() instanceof Player entityPlayer && waitingForLogin(entityPlayer);
	}
}
