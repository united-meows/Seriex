package pisi.unitedmeows.seriex.auth;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

import pisi.unitedmeows.eventapi.event.listener.Listener;
import pisi.unitedmeows.pispigot.Pispigot;
import pisi.unitedmeows.pispigot.event.impl.client.C14PacketTabComplete;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.impl.server.AuthConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.inventories.LoginInventory;
import pisi.unitedmeows.seriex.util.title.AnimatedTitle;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.parallel.Async;

// TODO finish
public class AuthListener extends Manager implements org.bukkit.event.Listener {
	private Map<PlayerW, AuthInfo> playerMap = new HashMap<>();
	private Method getShooter;
	private Method subscribe;
	private Pispigot pispigot;
	private boolean shooterIsLivingEntity;

	@Override
	public void start(Seriex seriex) {
		Bukkit.getPluginManager().registerEvents(this, Seriex.get());
		try {
			this.getShooter = Projectile.class.getDeclaredMethod("getShooter");
			this.shooterIsLivingEntity = (this.getShooter.getReturnType() == LivingEntity.class);
		}
		catch (NoSuchMethodException
					| SecurityException e) {
			seriex.logger().fatal("Cannot load getShooter() method on Projectile class", e);
		}
	}

	@EventHandler
	public void onPreJoin(AsyncPlayerPreLoginEvent event) {}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!canApplyProtection(player)) {
			// TODOH set player state to default when minigames are added
			computeAuthInfo(player);
			Seriex.logger().debug("[LOGIN] - computed auth info");
			player.teleport(getServerConfig().getWorldSpawn());
			Seriex.logger().debug("[LOGIN] - teleported to world spawn");
		}
		Pispigot.playerSystem(player).subscribeAll(this);
		Seriex.logger().debug("[LOGIN] - subscribed to pispigot");
	}

	@Override
	public void cleanup() throws SeriexException {
		playerMap.forEach((PlayerW player, AuthInfo info) -> info.onServerEnd());
		playerMap.clear();
	}

	public void stopAuthentication(PlayerW playerW) {
		Pispigot.playerSystem(playerW.getHooked()).unsubscribeAll(this);
		final AuthInfo authentication = playerMap.get(playerW);
		authentication.onLogin();
		Seriex.logger().debug("[LOGIN] - stopping auth");
	}

	protected static String[] cachedWelcome = null;

	public class AuthInfo extends HookClass<PlayerW> {
		public final Location spawnLocation = getServerConfig().getWorldSpawn();
		private long startMS = System.currentTimeMillis() , endMS;
		private AuthState state = AuthState.WAITING;
		private long benchmark;
		private BukkitRunnable joinMessageRunnable;

		public void onJoin() {
			PlayerW baseHook = getHooked();
			baseHook.denyMovement();
			Player player = baseHook.getHooked();
			Seriex.get().inventoryPacketAdapter().sendBlankInventoryPacket(player);
			LoginInventory.open(baseHook, Seriex.get().authentication());
			if (cachedWelcome == null) {
				// TODO translations
				cachedWelcome = AnimatedTitle.animateText("Welcome to Seriex!", "Seriex", "&d", "&5&l");
				Seriex.logger().debug("[LOGIN] - cached welcome");
			}
			joinMessageRunnable = AnimatedTitle.animatedTitle(player, cachedWelcome, null);
			if (baseHook.isGuest() && false) {
				// TODO translations & guest support
				Seriex.get().msg(player, "You automatically logged in because you are in a guest account!");
				onLogin();
			}
		}

		public void onLogin() {
			Seriex.logger().debug("onLogin?");
			PlayerW baseHook = getHooked();
			baseHook.allowMovement();
			baseHook.getHooked().updateInventory();
			state = AuthState.LOGGED_IN;
			joinMessageRunnable.run();
			remove();
		}

		public void onServerEnd() {
			remove();
		}

		public void onAuthInterrupted() {
			remove();
		}

		private void remove() {
			AuthListener authentication = Seriex.get().authentication();
			PlayerW hook = getHooked();
			if (authentication.playerMap.containsKey(hook)) {
				authentication.playerMap.remove(hook);
			}
		}

		public AuthInfo(PlayerW player) {
			this.hooked = player;
		}

		@Override
		public PlayerW getHooked() {
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

	public AuthInfo computeAuthInfo(Player player) {
		return playerMap.computeIfAbsent(Seriex.get().dataManager().user(player), computedPlayer -> {
			AuthInfo authInfo = new AuthInfo(computedPlayer);
			authInfo.onJoin();
			playerMap.put(computedPlayer, authInfo);
			return authInfo;
		});
	}

	public boolean canApplyProtection(Player player) {
		return getAuthInfo(player) != null;
	}

	public boolean waitingForLogin(Player player) {
		AuthInfo authInfo = getAuthInfo(player);
		return canApplyProtection(player) && authInfo.state == AuthState.WAITING;
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

	public Listener<C14PacketTabComplete> tabcompleteListener = new Listener<>(event -> {
		Player player = event.player();
		if (waitingForLogin(player)) {
			event.cancel(true);
			event.setSilentCancel(true); // does this do smth i dont remember lololol @slowcheet4h
		}
	});

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player)) {
			String cmd = event.getMessage().split(" ")[0].toLowerCase();
			AuthConfig authConfig = getAuthConfig();
			if (!authConfig.ALLOWED_COMMANDS.value().contains(cmd)) return;
			event.setCancelled(true);
			// TODO set ("auth.command_not_allowed") in TranslationConfig
			// The command %s is not allowed! <- default message
			String value = Seriex.get().I18n().getString("auth.command_not_allowed", Seriex.get().dataManager().user(player));
			Seriex.get().msg(player, value);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (waitingForLogin(player)) {
			event.setCancelled(true);
			// TODO set ("auth.chat_not_allowed") in TranslationConfig
			// In order to chat you must be authenticated! <- default message
			String value = Seriex.get().I18n().getString("auth.chat_not_allowed", Seriex.get().dataManager().user(player));
			Seriex.get().msg(player, value);
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
		Location spawnLocation = getAuthInfo(player).spawnLocation;
		if (spawnLocation.distance(from) > getAuthConfig().ALLOWED_MOVEMENT_DELTA.value()) {
			player.teleport(spawnLocation);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!waitingForLogin(event.getPlayer())) return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!waitingForLogin(event.getPlayer())) return;
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.getEntity().setFireTicks(0);
			event.setDamage(0.0D);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onAttack(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getDamager())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setTarget(null);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void entityRegainHealthEvent(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setAmount(0.0D);
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.HIGHEST)
	public void onEntityInteract(EntityInteractEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onLowestEntityInteract(EntityInteractEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() == null) return;
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
				Seriex.logger().fatal("Error getting shooter %s", e.getMessage());
			}
		} else {
			shooterRaw = projectile.getShooter();
		}
		if (shooterRaw instanceof Player && this.waitingForLogin((Player) shooterRaw)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.NORMAL)
	public void onShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player && waitingForLogin((Player) event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		// ( + ) %s %join_message%
		// Default join message: "joined the server!"
		// TODO set ("join_message") in TranslationConfig
		event.setJoinMessage(null);
		Seriex.get().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
			String value = Seriex.get().I18n().getString("join_message", Seriex.get().dataManager().user(onlinePlayer));
			Seriex.get().msg(onlinePlayer, "&7( &a+ &7) " + value);
		});
		Async.async_w(() -> {
			Location worldSpawn = getServerConfig().getWorldSpawn();
			if (event.getPlayer() != null && event.getPlayer().isOnline() && worldSpawn != null) {
				event.getPlayer().teleport(worldSpawn);
			}
		}, 50 * 1L /* 1 tick */);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		// ( - ) %leave_message%
		// Default leave message: "%s left the server!"
		// TODO set ("leave_message") in TranslationConfig
		event.setQuitMessage(null);
		Seriex.get().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
			String value = Seriex.get().I18n().getString("leave_message", Seriex.get().dataManager().user(onlinePlayer));
			Seriex.get().msg(onlinePlayer, "&7( &c- &7) " + value);
		});
		if (!waitingForLogin(player)) return;
		getAuthInfo(player).onAuthInterrupted();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(PlayerKickEvent event) {
		if (event.getReason().contains("You logged in from another location")) {
			event.setCancelled(true);
			return;
		}
		Player player = event.getPlayer();
		if (!waitingForLogin(player)) return;
		getAuthInfo(player).onAuthInterrupted();
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
		if (player instanceof Player) {
			if (waitingForLogin((Player) player)) {
				event.setCancelled(true);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Seriex.get(), player::closeInventory, 1L);
			}
		} else {
			Seriex.logger().fatal("göte geldik HumanEntity Playerin Master Classi olmuyomuş yardım edin");
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		if (whoClicked instanceof Player) {
			if (waitingForLogin((Player) whoClicked)) {
				event.setCancelled(true);
			}
		} else {
			Seriex.logger().fatal("HumanEntity is not instanceof Player... @ghost pls fix.");
		}
	}

	@EventHandler(ignoreCancelled = true , priority = EventPriority.LOWEST)
	public void onPlayerHitPlayerEvent(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player && waitingForLogin((Player) entity)) {
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
		if (!waitingForLogin(event.getPlayer())) return;
		event.setRespawnLocation(getServerConfig().getWorldSpawn());
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
}
