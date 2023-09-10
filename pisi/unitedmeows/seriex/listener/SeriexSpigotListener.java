package pisi.unitedmeows.seriex.listener;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofDays;
import static org.bukkit.event.inventory.InventoryType.BREWING;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST;

import java.time.Duration;
import java.util.*;
import java.util.function.DoubleConsumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerLogin;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer.Settings;
import pisi.unitedmeows.seriex.util.MaintainersUtil;
import pisi.unitedmeows.seriex.util.config.single.impl.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.BannedCommandsConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.crasher.PlayerCrasher;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.ip.IPApi;
import pisi.unitedmeows.seriex.util.ip.IPApiResponse;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;

public class SeriexSpigotListener implements Listener {
	private static final Map<String, IPApiResponse> responseCache = new HashMap<>();
	public static final List<ServerChatMessage> serverChatMessages = new ArrayList<>();
	private HashMap<UUID, Long> chatDelay = new HashMap<>();
	private Map<Item, Long> itemTracker;
	private long deleteMS;

	public record ServerChatMessage(String author, String message, long timestamp) {}


	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(final PlayerJoinEvent event) {
		var player = event.getPlayer();
		var name = player.getName();
		if (!player.isOnline()) {
			Seriex.get().kick(player, Messages.SERVER_INTERNAL_ERROR);
			return;
		}

		var playerStruct = Seriex.get().database().getPlayer(name);
		if (playerStruct.banned) {
			handleBan(event, player);
			return;
		}

		var hooked = Seriex.get().dataManager().user(player);
		var ip = hooked.getIP();
		if (playerStruct.loginCounter == 0) {
			var response = responseCache.get(ip);
			var playerSettings = Seriex.get().database().getPlayerSettings(player.getName());
			playerSettings.selectedLanguage = response == null
						/* probably using loki */
						? Language.ENGLISH.languageCode()
						: Language.fromCode(response.getCountryCode(), Language.ENGLISH).languageCode();
			if (hooked.isGuest()) { // TODO guest support
				Seriex.get().kick(player, Messages.LOGIN_ABORT_MCP);
				return;
			}
			// default settings
			playerSettings.guest = hooked.isGuest();
			playerSettings.fall_damage = false;
			playerSettings.flags = true;
			playerSettings.hunger = false;
			playerStruct.rank_name = (MaintainersUtil.isMaintainer(name) ? Ranks.MAINTAINER : Ranks.TESTER).internalName();
			playerSettings.update();
		}

		var playerLogin = new StructPlayerLogin();
		playerLogin.player_id = playerStruct.player_id;
		playerLogin.ip_address = ip;
		playerLogin.ms = Seriex.get().fixedMS();
		playerLogin.create();

		playerStruct.loginCounter++;
		hooked.newPlayMS();
		playerStruct.update();
		Seriex.get().logger().info("{} joined the server!", name);
		Seriex.get().discordBot().addMessageToQueue(String.format("%s joined the server!", name));
		hooked.updateNametag();
		Seriex.get().scoreboardManager().onJoin(player);
		Seriex.get().areaManager().areaList.forEach(area -> {
			area.join(player);
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) { // uuid spoof fix
		var player = event.getPlayer();
		var offlinePlayerName = "OfflinePlayer:" + player.getName();
		var calculatedUUID = UUID.nameUUIDFromBytes(offlinePlayerName.getBytes(UTF_8));
		var uniqueId = player.getUniqueId();
		if (!calculatedUUID.equals(uniqueId)) {
			event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7Disallowed UUID!\n&cCalculated UUID: %s\n&4Login UUID: %s", Seriex.get().suffix(), calculatedUUID, uniqueId)));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPreJoin(AsyncPlayerPreLoginEvent event) {
		var suffix = Seriex.get().suffix();
		if (!Seriex.get().doneInitializing()) {
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7Loading server...", suffix)));
			return;
		}
		var name = event.getName();
		var length = name.length();
		if (length < 3 || length > 16) {
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7Disallowed username.", suffix)));
			return;
		}
		var database = Seriex.get().database();
		var playerStruct = database.getPlayer(name);
		// TODO guest support
		if (playerStruct == null) {
			var discordConfig = (DiscordConfig) Seriex.get().fileManager().config(DiscordConfig.class);
			var discordLink = discordConfig.INVITE_LINK.value();
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7Please register on the discord server. \n%s", suffix, discordLink)));
			return;
		}
		var player = Seriex.get().plugin().getServer().getPlayerExact(name);
		if (player != null) {
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7%s", "Player is already online!", suffix)));
			return;
		}
		var hostAddress = event.getAddress().getHostAddress();
		responseCache.computeIfAbsent(hostAddress, IPApi::response);
		var response = responseCache.get(hostAddress);
		var fuckMCLeaks = "OVH SAS".equals(response.getIsp()) && ("France".equalsIgnoreCase(response.getCountry()) || "Italy".equalsIgnoreCase(response.getCountry()));
		var noChina = "CN".equals(response.getCountryCode());
		if (fuckMCLeaks) {
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7%s", "MC-Leaks is not allowed on Seriex.", suffix)));
			return;
		}
		if (noChina) {
			// Chinese IP`s are literally never seen in Seriex because they cannot connect
			// for whatever reason, so blocking them changes nothing.
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7%s", "Chinese IPs are not allowed on Seriex.", suffix)));
			return;
		}
		if (Seriex.get().maintenance()) {
			event.disallow(KICK_WHITELIST, Seriex.colorizeString(String.format("%s\n&7%s", "Seriex is on maintenance.", suffix)));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		var player = event.getPlayer();
		if (!player.isOnline())
			return;

		var user = Seriex.get().dataManager().user(player);
		var bannedCommandsConfig = (BannedCommandsConfig) Seriex.get().fileManager().config(BannedCommandsConfig.class);
		var message = event.getMessage();

		if (bannedCommandsConfig.isConsoleOnly(message)) {
			Seriex.get().msg(player, Messages.COMMAND_CONSOLE_ONLY);
			event.setCancelled(true);
			return;
		}

		/* we do not care if the player has bukkit operator status */
		if (bannedCommandsConfig.isMaintainerOnly(message) && !user.rank().equals(Ranks.MAINTAINER)) {
			Seriex.get().msg(player, Messages.COMMAND_BANNED);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player player) {
			var user = Seriex.get().dataManager().user(player);
			if (event.getEntity() instanceof LivingEntity livingEntity) {
				var virtualPlayer = Seriex.get().virtualPlayerManager().fromEntityID(livingEntity.getEntityId());
				if (virtualPlayer != null && virtualPlayer.setting(Settings.AREA))
					return;

				var cancelForVirtualPlayer = false;
				if (livingEntity.getNoDamageTicks() <= livingEntity.getMaximumNoDamageTicks() / 2) {
					user.reset_attacks();
				} else {
					user.updateAttacks(user.attacks() + 1);
					if (user.attacks() > 4 && !user.hasDurabilityPatchBypass()) {
						event.setCancelled(cancelForVirtualPlayer = true);
						user.updateAttacks(4);
					}
				}
				if (virtualPlayer != null) {
					if (!cancelForVirtualPlayer)
						virtualPlayer.fakeDamage(true);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAutoComplete(PlayerChatTabCompleteEvent event) {
		var user = Seriex.get().dataManager().user(event.getPlayer());
		/*
		 * Suggester suggester = user.selectedLanguage().getSuggester();
		 * if (suggester != null) {
		 * List<String> autocomplete = suggester.autocomplete(event.getLastToken(), 5);
		 * List<String> quickFix = suggester.suggestions(event.getLastToken(), 5);
		 * event.getTabCompletions().addAll(quickFix);
		 * }
		 */
	}

	private boolean handleSoup(double val, double max, double add, DoubleConsumer valueConsumer) {
		var set = false;
		var value = -1.0;
		if (val < max && val > 0) {
			if (val < max - add + 1) {
				value = val + add;
			} else if (val > max - add) {
				value = max;
			}
			valueConsumer.accept(value);
			set = true;
		}
		return set;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEvent interactEvent) {
		var player = interactEvent.getPlayer();
		var action = interactEvent.getAction();
		if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
					&& player.getItemInHand() != null
					&& player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
			var soupHealth = handleSoup(player.getHealth(), player.getMaxHealth(), 7, player::setHealth);
			var soupFood = handleSoup(player.getFoodLevel(), 20, 7, v -> player.setFoodLevel((int) v));
			if (soupFood || soupHealth) {
				var newItem = ItemBuilder.of(Material.BOWL).build();
				if (player.getItemInHand().getAmount() > 1) {
					var itemInHand = player.getItemInHand();
					itemInHand.setAmount(itemInHand.getAmount() - 1);
					newItem = itemInHand;
				}
				player.setItemInHand(newItem);
				player.updateInventory();
				return;
			}
		}
		var clickedBlock = interactEvent.getClickedBlock();
		if (clickedBlock == null)
			return;
		var type = clickedBlock.getType();
		if (type == null || action == null)
			return;
		if (type != Material.SIGN && type != Material.SIGN_POST && type != Material.WALL_SIGN)
			return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
			return;
		// no more scary npe!!! (inshallah :pray: :kaaba:)
		var sign = (Sign) clickedBlock.getState();
		try {
			var line1 = ChatColor.stripColor(sign.getLine(0)).substring(1);
			line1 = line1.substring(0, line1.length() - 1);
			var signCommands = Seriex.get().signManager().signCommands();
			for (SignCommand signCommand : signCommands) {
				if (signCommand.trigger().equalsIgnoreCase(line1)) {
					var signMaterial = (org.bukkit.material.Sign) sign.getData();
					if (action == Action.RIGHT_CLICK_BLOCK)
						signCommand.runRight(Seriex.get().dataManager().user(player), sign, signMaterial);
					else if(!player.isSneaking())
						signCommand.runLeft(Seriex.get().dataManager().user(player), sign, signMaterial);
					interactEvent.setCancelled(true);
					break;
				}
			}
		}
		catch (Exception e) {
			// TODO: handle
		}
	}

	private void onQuit(Player player) {
		var user = Seriex.get().dataManager().user(player, false);
		if (user == null) {
			Seriex.get().logger().error("{} quit without having wrapper!", player.getName());
			return;
		}
		for (HumanEntity viewer : player.getInventory().getViewers()) {
			if (viewer instanceof Player playerViewer) {
				var uviewer = Seriex.get().dataManager().user(playerViewer);
				if (uviewer.invsee())
					uviewer.hook().closeInventory();
			}
		}
		user.handlePlayMS();
		Seriex.get().plugin().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
			Seriex.get().msg(onlinePlayer, Messages.AUTO_LEAVE_MESSAGE, player.getName());
		});
		Seriex.get().discordBot().addMessageToQueue(String.format("%s left the server!", player.getName()));
		var currentMinigame = user.currentMinigame();
		if (currentMinigame != null)
			currentMinigame.onLeave(user);
		Seriex.get().areaManager().areaList.stream()
					.filter(area -> area.isInside(player))
					.forEach(area -> area.disconnect(player));
		Seriex.get().scoreboardManager().onQuit(player);
		Seriex.get().dataManager().removeUser(player);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		var user = Seriex.get().dataManager().user((Player) event.getWhoClicked());
		var top = event.getView().getTopInventory();
		var type = top.getType();
		if (user.invsee()) {
			var isSpecialChest = type == InventoryType.CHEST && top.getSize() == 9;
			if (type == InventoryType.PLAYER || isSpecialChest) {
				var invHolder = top.getHolder();
				if (invHolder instanceof HumanEntity) {
					event.setCancelled(true);
					Seriex.get().runLater(user.hook()::updateInventory, 1);
				}
			} else if (type == InventoryType.ENDER_CHEST) {
				event.setCancelled(true);
				Seriex.get().runLater(user.hook()::updateInventory, 1);
			}
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		var stack = event.getItem().getItemStack();
		if (stack.getType() != Material.POTION)
			return;

		event.setCancelled(true);
		if (event.getPlayer().getInventory().firstEmpty() == -1)
			return;

		event.getPlayer().getInventory().addItem(stack);
		event.getItem().remove();
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		var inventory = event.getView().getTopInventory();
		if (inventory == null || inventory.getType() != BREWING)
			return;
		if (event.getClick() == ClickType.NUMBER_KEY && (event.getRawSlot() == 0 || event.getRawSlot() == 1 || event.getRawSlot() == 2)) {
			event.setCancelled(true);
			return;
		}
		if (event.getClick().name().contains("SHIFT") && event.getCurrentItem().getAmount() > 1) {
			final var player = (Player) event.getWhoClicked();
			final var stack = event.getCurrentItem();
			final var newStack = new ItemStack(stack);
			newStack.setAmount(stack.getAmount() - 1);
			stack.setAmount(1);
			Seriex.get().run(() -> {
				if (player.getInventory().getItem(event.getSlot()) == null) {
					player.getInventory().setItem(event.getSlot(), newStack);
				} else {
					stack.setAmount(newStack.getAmount() + 1);
				}
				player.updateInventory();
			});
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		var top = event.getView().getTopInventory();
		var type = top.getType();
		var isSpecialChest = type == InventoryType.CHEST && top.getSize() == 9;
		if (type == InventoryType.PLAYER || isSpecialChest || type == InventoryType.ENDER_CHEST) {
			var user = Seriex.get().dataManager().user((Player) event.getPlayer());
			user.invsee(false);
			Seriex.get().runLater(user.hook()::updateInventory, 1);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onLeave(final PlayerQuitEvent event) {
		var player = event.getPlayer();
		this.onQuit(player);
		event.setQuitMessage("");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onKick(final PlayerKickEvent event) {
		var player = event.getPlayer();
		this.onQuit(player);
		event.setLeaveMessage("");
	}

	// FIXME: replace vanilla messages
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent event) {
		var playerThatDied = event.getEntity();
		var user = Seriex.get().dataManager().user(playerThatDied);
		if (user.playerState() == PlayerState.MINIGAMES) {
			user.currentMinigame().onPlayerDeath(event);
		}

		event.setDeathMessage(null);
	}

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if (itemTracker == null) {
			itemTracker = new HashMap<>();
			Seriex.get().plugin().getServer().getScheduler().runTaskTimer(Seriex.get().plugin(), () -> {
				var iterator = itemTracker.keySet().iterator();
				while (iterator.hasNext()) {
					var item = iterator.next();
					long timeDropped = itemTracker.get(item);
					if (timeDropped + Duration.ofSeconds(15).toMillis() < System.currentTimeMillis()) {
						item.remove();
						iterator.remove();
					}
				}
			}, 10L, 20L * 10);
		}

		itemTracker.put(event.getEntity(), System.currentTimeMillis());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
		var user = Seriex.get().dataManager().user(event.getPlayer());
		if (user.playerState() == PlayerState.MINIGAMES) {
			user.currentMinigame().onPlayerInteract(event);
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent event) {
		var entity = event.getEntity();
		if (entity instanceof Player playerEntity) {
			var user = Seriex.get().dataManager().user(playerEntity);
			var playerState = user.playerState();
			if (playerState == PlayerState.MINIGAMES) {
				user.currentMinigame().onHungerChange(event);
			} else if (playerState == PlayerState.SPAWN && !user.playerSettings().hunger) {
				event.setFoodLevel(20);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		var entity = event.getEntity();
		if (entity instanceof Player player) {
			var virtualPlayer = Seriex.get().virtualPlayerManager().fromEntityID(player.getEntityId());
			if (virtualPlayer != null)
				return;
			var user = Seriex.get().dataManager().user(player);
			var playerState = user.playerState();
			if (playerState == PlayerState.SPAWN
						&& event.getCause() == EntityDamageEvent.DamageCause.FALL
						&& !user.playerSettings().fall_damage) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
	public void onPlayerMoveMonitor(PlayerMoveEvent event) {
		Seriex.get().playerLogger().logPlayers(event);
		var user = Seriex.get().dataManager().user(event.getPlayer());

		if (user.afk()) {
			user.afk(false);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		var user = Seriex.get().dataManager().user(event.getPlayer());
		if (user.playerState() == PlayerState.MINIGAMES) {
			user.currentMinigame().onPlayerMove(event);
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		var user = Seriex.get().dataManager().user(event.getPlayer());
		if (user.playerState() == PlayerState.MINIGAMES) {
			user.currentMinigame().onPlayerDropItem(event);
		}
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		var user = Seriex.get().dataManager().user(event.getPlayer());
		if (user.playerState() == PlayerState.MINIGAMES) {
			user.currentMinigame().onPlayerItemConsume(event);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		var player = Seriex.get().dataManager().user(event.getPlayer());
		if (player.playerState() == PlayerState.MINIGAMES) {
			event.setRespawnLocation(player.currentMinigame().spawnLocation);
			player.currentMinigame().onRespawn(player);
		} else if (player.playerState() == PlayerState.SPAWN) {
			var srvConfig = (ServerConfig) Seriex.get().fileManager().config(ServerConfig.class);
			event.setRespawnLocation(srvConfig.getWorldSpawn());
		}
	}



	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncChat(final AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;

		var player = event.getPlayer();
		var uniqueId = player.getUniqueId();
		if (!chatDelay.containsKey(uniqueId)) {
			chatDelay.put(uniqueId, System.currentTimeMillis());
		} else if (System.currentTimeMillis() - chatDelay.get(uniqueId) >= 1000) {
			chatDelay.remove(uniqueId);
		} else {
			Seriex.get().msg(player, Messages.SPAM_SLOWDOWN);
			event.setCancelled(true);
		}

		if(System.currentTimeMillis() - deleteMS > Duration.ofMinutes(5).toMillis()) {
			serverChatMessages.clear();
			deleteMS = System.currentTimeMillis();
		}

		var name = player.getName();
		var user = Seriex.get().dataManager().user(player);
		var stringBuilder = new StringBuilder("(");
		if (user.playerState() == PlayerState.MINIGAMES) {
			stringBuilder.append(user.currentMinigame().name.toUpperCase(Locale.ENGLISH));
		} else stringBuilder.append(user.playerState().name());
		stringBuilder.append(") ");
		stringBuilder.append("[");
		stringBuilder.append(user.rank().internalName().toUpperCase(Locale.ENGLISH));
		stringBuilder.append("] ");
		stringBuilder.append(name);
		stringBuilder.append(" => ");
		stringBuilder.append(Seriex.colorizeString(event.getMessage()));

		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
		for (Player onlinePlayer : onlinePlayers) {
			if (onlinePlayer == player)
				continue;

			var otherUser = Seriex.get().dataManager().user(onlinePlayer);

			if (otherUser.isIgnored(name)) {
				event.getRecipients().remove(onlinePlayer);
			}
		}

		serverChatMessages.add(new ServerChatMessage(name, event.getMessage(), System.currentTimeMillis()));
		Seriex.get().discordBot().addMessageToQueue(stringBuilder.toString());
		event.setFormat(user.rankData().generateFormat());
	}

	private void handleBan(PlayerJoinEvent event, Player player) {
		var name = player.getName();
		Seriex.get().logger().info("{} is banned.", name);
		var banActionsConfig = (BanActionsConfig) Seriex.get().fileManager().config(BanActionsConfig.class);
		boolean loginDisable = banActionsConfig.DISABLE_LOGIN.value();
		if (loginDisable) {
			var stringBuilder = new StringBuilder();
			boolean trollLogin = banActionsConfig.LOGIN_TROLL.value();
			var times = 70; // TODO test
			var lineSeperator = "\n";
			if (trollLogin) {
				for (var i = 0; i < times; i++) {
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
			if (trollLogin) {
				for (var i = 0; i < times; i++) {
					stringBuilder.append(lineSeperator);
				}
			}
			event.getPlayer().kickPlayer(Seriex.colorizeString(stringBuilder.toString()));
		}
		if (Boolean.TRUE.equals(banActionsConfig.ANNOUNCE_IP_ON_JOIN.value())) {
			Seriex.get().plugin().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
				Seriex.get().msg(onlinePlayer, Messages.BAN_ACTIONS_ANNOUNCE_IP, name,
							Seriex.get().dataManager().user(player).getMaskedIP());
			});
		}
		if (Boolean.TRUE.equals(banActionsConfig.DISABLE_DISCORD.value())) {
			var JDA = Seriex.get().discordBot().JDA();
			var playerStructDiscord = Seriex.get().database().getPlayerDiscord(name);
			var discordConfig = (DiscordConfig) Seriex.get().fileManager().config(DiscordConfig.class);
			var guild = JDA.getGuildById(discordConfig.ID_GUILD.value());
			var member = guild.getMember(UserSnowflake.fromId(playerStructDiscord.snowflake));
			// TODOH should all bans be 7 days long?
			if (member == null) {
				Seriex.get().logger().error("Can`t timeout from discord because member is null!");
			} else if (!member.isTimedOut()) {
				try {
					member.timeoutFor(ofDays(7))
								.reason("Banned from the server.")
								.complete();
				}
				catch (Exception e) {
					Seriex.get().logger().error("Can`t timeout from discord because: " + e.getLocalizedMessage());
				}
			}
		}
		if (Boolean.TRUE.equals(banActionsConfig.CRASH_GAME.value()) && !loginDisable) {
			PlayerCrasher.fuck(player);
		}
	}
}
