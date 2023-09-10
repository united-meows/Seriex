package pisi.unitedmeows.seriex.commands;

import static java.util.concurrent.TimeUnit.*;
import static org.bukkit.Material.*;
import static org.bukkit.enchantments.Enchantment.DAMAGE_ALL;
import static org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.Potion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.rollczi.litecommands.argument.Arg;
import dev.rollczi.litecommands.argument.joiner.Joiner;
import dev.rollczi.litecommands.argument.option.Opt;
import dev.rollczi.litecommands.command.execute.ArgumentExecutor;
import dev.rollczi.litecommands.command.execute.Execute;
import dev.rollczi.litecommands.command.root.RootRoute;
import org.bukkit.potion.PotionType;
import panda.std.Option;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.util.Permissions;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.inventories.ui.*;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW.Attributes;

import dev.rollczi.litecommands.command.section.CommandSection;

@RootRoute
public class GeneralCommands {

	@Execute(route = "help")
	public void help(PlayerW sender) {
		var seriex = Seriex.get();
		Collection<CommandSection<CommandSender>> sections = seriex.commandManager().getCommandService().getSections();
		seriex.msg_no_translation(sender,
					sections.stream()
								.map(CommandSection::getName)
								.distinct()
								.collect(seriex.collector())
		);
	}

	@Execute(route = "login", aliases = {
				"log", "l"
	}, required = 1)
	public void login(PlayerW sender, @Arg String text) {
		var authManager = Seriex.get().authentication();
		var authInfo = authManager.getAuthInfo(sender.hook());

		if (authInfo != null) {
			var hashedString = Hashing.hashedString(sender.attribute(Attributes.SALT) + text);
			var hashedPassword = sender.attribute(Attributes.PASSWORD);
			if (Objects.equals(hashedString, hashedPassword)) {
				authManager.stopAuthentication(authInfo);
			}
		}
	}

	@Execute(route = "clear", aliases = {
				"clearinv"
	}, min = 0, max = 1)
	public void clear(PlayerW sender, @Opt Option<PlayerW> other) {
		var inventory = (sender.rank().operator() ? other.orElseGet(sender) : sender).hook().getInventory();
		inventory.setArmorContents(null);
		inventory.clear();
	}

	@Execute(route = "plugins", aliases = "pl")
	public void plugins(PlayerW sender) {
		var plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
					.map(p -> (p.isEnabled() ? "&a" : "&c") + p.getName())
					.collect(Collectors.joining("&8, ", "&7[", "&7]"));
		Seriex.get().msg(sender, Messages.COMMAND_PLUGINS, plugins);
	}

	@Execute(route = "seriex", aliases = "menu")
	public void seriexMenu(PlayerW sender) {
		MainGUI.INVENTORY.open(sender.hook());
	}

	@Execute(route = "kit", max = 0)
	public void kit(PlayerW sender) {
		var player = sender.hook();

		if (sender.disallowCommand()) return;

		Kit.createKit(ItemBuilder.of(DIAMOND_SWORD).max_enchantment(DAMAGE_ALL).build(), new ItemStack[] {
					ItemBuilder.of(DIAMOND_HELMET).max_enchantment(PROTECTION_ENVIRONMENTAL).build(),
					ItemBuilder.of(DIAMOND_CHESTPLATE).max_enchantment(PROTECTION_ENVIRONMENTAL).build(),
					ItemBuilder.of(DIAMOND_LEGGINGS).max_enchantment(PROTECTION_ENVIRONMENTAL).build(),
					ItemBuilder.of(DIAMOND_BOOTS).max_enchantment(PROTECTION_ENVIRONMENTAL).build()
		}, ItemBuilder.of(GOLDEN_CARROT).amount(64).build()).clearAndGive(player);
		Seriex.get().msg(player, Messages.COMMAND_KIT_EQUIP);
	}

	private final Cache<UUID, UUID> replies = CacheBuilder.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build();

	@Execute(route = "msg", aliases = {
				"message", "m", "whisper", "tell", "t"
	}, min = 2)
	public void msgCommand(PlayerW sender, @Arg PlayerW target, @Joiner String message) {
		var baseString = "&8[&d%s &7=> &5%s&8] &f* &7%s";
		Seriex.get().msg_no_translation(sender, baseString, sender.attribute(Attributes.NAME), target.attribute(Attributes.NAME), message);
		Seriex.get().msg_no_translation(target, baseString, sender.attribute(Attributes.NAME), target.attribute(Attributes.NAME), message);

		this.replies.put(target.uuid(), sender.uuid());
		this.replies.put(sender.uuid(), target.uuid());
	}

	@Execute(route = "reply", aliases = {
				"r"
	}, min = 1)
	public void reply(PlayerW sender, @Joiner String message) {
		var baseString = "&8[&d%s &7=> &5%s&8] &f* &7%s";
		var uuid = this.replies.getIfPresent(sender.uuid());
		var player = Bukkit.getPlayer(uuid);
		if (player == null) {
			Seriex.get().msg_no_translation(sender, "Player not found.");
			return;
		}

		var fromUUID = Seriex.get().dataManager().user(player);

		Seriex.get().msg_no_translation(sender, baseString, sender.attribute(Attributes.NAME), fromUUID.attribute(Attributes.NAME), message);
		Seriex.get().msg_no_translation(fromUUID, baseString, sender.attribute(Attributes.NAME), fromUUID.attribute(Attributes.NAME), message);
	}

	@Execute(route = "afk")
	public void afk(PlayerW sender) {
		sender.afk(!sender.afk());
		Seriex.get().msg(sender, sender.afk() ? Messages.COMMAND_AFK_ENABLE : Messages.COMMAND_AFK_DISABLE);
	}

	@Execute(route = "fly", aliases = {
				"flight",
	}, min = 0, max = 1)
	public void fly(PlayerW sender, @Opt Option<PlayerW> other) {
		var seriex = Seriex.get();

		var player = (sender.rank().operator() ? other.orElseGet(sender) : sender).hook();

		if (sender.doesntHaveRank(Ranks.VIP))
			return;

		var playerState = sender.playerState();
		if (playerState != PlayerState.SPAWN) {
			seriex.msg(player, Messages.COMMAND_WRONG_STATE, playerState.name());
			return;
		}

		if (player.getAllowFlight()) {
			seriex.msg(player, Messages.COMMAND_FLY_DISABLE);
			player.setAllowFlight(false);
			player.setFallDistance(0.0F);
			player.setFlying(false);
		} else {
			seriex.msg(player, Messages.COMMAND_FLY_ENABLE);
			player.setAllowFlight(true);
			player.setFallDistance(0.0F);
		}
	}

	@Execute(route = "feed", min = 0, max = 1)
	public void feed(PlayerW sender, @Opt Option<PlayerW> other) {
		var seriex = Seriex.get();

		var player = (sender.rank().operator() ? other.orElseGet(sender) : sender).hook();

		if (sender.disallowCommand()) return;

		if (player.isDead() || player.getHealth() <= 0.0D)
			return; // wut

		player.setFoodLevel(20);
		player.setSaturation(10F);
		player.setExhaustion(0F);
		if (player.getUniqueId().equals(sender.uuid()))
			seriex.msg(player, Messages.COMMAND_FEED_SELF_SUCCESS);
		else
			seriex.msg(player, Messages.COMMAND_FEED_OTHER_SUCCESS, sender.attribute(Attributes.NAME));
	}

	@Execute(route = "heal", min = 0, max = 1)
	public void heal(PlayerW sender, @Opt Option<PlayerW> other) {
		var seriex = Seriex.get();

		var player = (sender.rank().operator() ? other.orElseGet(sender) : sender).hook();

		if (sender.disallowCommand()) return;

		if (player.isDead() || player.getHealth() <= 0.0D)
			return; // wut

		var maxHealth = player.getMaxHealth();
		var health = player.getHealth();

		var amount = maxHealth - health;
		var newAmount = health + amount;

		if (newAmount > maxHealth)
			newAmount = maxHealth;

		player.setHealth(newAmount);
		player.setFoodLevel(20);
		player.setSaturation(10F);
		player.setExhaustion(0F);
		player.setFireTicks(0);

		if (player.getUniqueId().equals(sender.uuid()))
			seriex.msg(player, Messages.COMMAND_HEAL_SELF_SUCCESS);
		else
			seriex.msg(player, Messages.COMMAND_HEAL_OTHER_SUCCESS, sender.attribute(Attributes.NAME));
	}

	@Execute(route = "kill", min = 0, max = 1)
	public void kill(PlayerW sender, @Opt Option<PlayerW> other) {
		var seriex = Seriex.get();

		var player = (sender.rank().operator() ? other.orElseGet(sender) : sender).hook();
		if (sender.disallowCommand()) return;

		if (player.isDead() || player.getHealth() <= 0.0D)
			return; // wut

		seriex.plugin().getServer().getPluginManager().callEvent(new EntityDamageEvent(
					player,
					player.getPlayer().getName().equals(player.getName())
								? EntityDamageEvent.DamageCause.SUICIDE
								: EntityDamageEvent.DamageCause.CUSTOM,
					32767.0D));
		player.damage(32767.0D);
		if (player.getHealth() > 0.0D)
			player.setHealth(0.0D);

		if (player.getUniqueId().equals(sender.uuid()))
			seriex.msg(player, Messages.COMMAND_KILL_SELF_SUCCESS);
		else
			seriex.msg(player, Messages.COMMAND_KILL_OTHER_SUCCESS, sender.attribute(Attributes.NAME));
	}

	@Execute(route = "playtime", min = 0, max = 1)
	public void playtime(PlayerW sender, @Opt Option<PlayerW> other) {
		var playerToLook = other.orElseGet(sender);

		sender.handlePlayMS();
		var millis = playerToLook.playerInfo().playTime;
		var playTime = String.format("%02d:%02d:%02d",
					MILLISECONDS.toHours(millis),
					MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(MILLISECONDS.toHours(millis)),
					MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis)));
		Seriex.get().msg(sender, Messages.COMMAND_PLAYTIME, playTime);
	}

	@Execute(route = "pay", required = 2)
	public void pay(PlayerW sender, @Arg PlayerW other, @Arg Integer amount) {
		Seriex seriex = Seriex.get();

		if (amount <= 0)
			amount = 0;

		if (amount > sender.playerWallet().coins) {
			seriex.msg(sender, Messages.COMMAND_PAY_INSUFFICIENT_FUNDS);
			return;
		}

		sender.takeMoney(amount);
		other.giveMoney(amount);
	}

	@Execute(route = "ignore", min = 0, max = 1)
	public void ignore(PlayerW sender, @Opt Option<PlayerW> other) {
		Seriex seriex = Seriex.get();

		if (other.isDefined()) {
			String name = other.get().attribute(Attributes.NAME);
			boolean ignored = sender.ignore(name);
			seriex.msg(sender, ignored ? Messages.COMMAND_IGNORE_IGNORING_PLAYER : Messages.COMMAND_IGNORE_NO_LONGER_IGNORING_PLAYER, name);
			return;
		}

		seriex.msg(sender, Messages.COMMAND_IGNORE_IGNORED_PLAYERS, sender.ignored().stream().collect(seriex.collector()));
	}

	@Execute(route = "stack", aliases = {
				"pot"
	})
	public void stacks(PlayerW sender) {
		Seriex seriex = Seriex.get();
		Player hook = sender.hook();

		Map<ItemStack, Integer> potionMap = new LinkedHashMap<>();
		PlayerInventory inventory = hook.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = inventory.getItem(i);
			if (item != null &&
						item.getType() == Material.POTION && !Potion.fromItemStack(item).isSplash() && item.getDurability() != 0) {
				ItemStack contains = null;
				for (ItemStack stack : potionMap.keySet()) {
					if (stack.getDurability() == item.getDurability() && stack.getItemMeta().equals(item.getItemMeta())) {
						contains = stack;
						break;
					}
				}
				if (contains != null) {
					potionMap.put(contains, potionMap.get(contains) + item.getAmount());
				} else {
					potionMap.put(item, item.getAmount());
				}
			}
		}

		if (potionMap.isEmpty()) {
			seriex.msg(sender, Messages.COMMAND_STACK_FAIL);
			return;
		}

		ItemStack[] items = inventory.getContents();
		for (int j = 0; j < items.length; j++) {
			if (items[j] != null && items[j].getType() == Material.POTION && !Potion.fromItemStack(items[j]).isSplash() && items[j].getDurability() != 0)
				inventory.clear(j);
		}

		for (var entry : potionMap.entrySet()) {
			ItemStack stack = entry.getKey();
			stack.setAmount(entry.getValue());
			inventory.addItem(stack);
		}

		hook.updateInventory();
		seriex.msg(sender, Messages.COMMAND_STACK_SUCCESS);
	}

	@Execute(route = "enchant", aliases = {
				"ench"
	}, required = 2)
	public void enchant(PlayerW sender, @Arg Enchantment enchantment, @Arg Integer level) {
		Seriex seriex = Seriex.get();

		if (sender.disallowCommand()) return;

		ItemStack itemInHand = sender.hook().getItemInHand();
		if (itemInHand == null
					|| Material.AIR.equals(itemInHand.getType())
					|| Material.ENCHANTED_BOOK.equals(itemInHand.getType())) {
			seriex.msg(sender, Messages.COMMAND_ENCHANT_NO_ITEM_IN_HAND);
			return;
		}

		int fixedLevel = sender.rank().operator()
					? Math.min(32767, Math.max(-32767, level))
					: Math.min(enchantment.getMaxLevel(), Math.max(enchantment.getStartLevel(), level));

		seriex.runLater(sender.hook()::updateInventory, 1);

		if (level == 0) {
			if (!itemInHand.containsEnchantment(enchantment)) {
				seriex.msg(sender, Messages.COMMAND_ENCHANT_HAS_NO_ENCHANT, enchantment.getName());
				return;
			}

			itemInHand.removeEnchantment(enchantment);
			seriex.msg(sender, Messages.COMMAND_ENCHANT_SUCCESS, enchantment.getName().toUpperCase(Locale.ENGLISH), 0);
			return;
		}

		if (itemInHand.containsEnchantment(enchantment)) {
			seriex.msg(sender, Messages.COMMAND_ENCHANT_HAS_ENCHANT, enchantment.getName());
			seriex.msg(sender, Messages.COMMAND_ENCHANT_REMOVE_TIP);
			return;
		}

		itemInHand.addUnsafeEnchantment(enchantment, fixedLevel);
		seriex.msg(sender, Messages.COMMAND_ENCHANT_SUCCESS, enchantment.getName().toUpperCase(Locale.ENGLISH), fixedLevel);
	}

	@Execute(route = "settings")
	public void settings(PlayerW sender) {
		SettingsGUI.INVENTORY.open(sender.hook());
	}

	@Execute(route = "areas")
	public void areas(PlayerW sender) {
		AreaGUI.INVENTORY.open(sender.hook());
	}

	@Execute(route = "minigames")
	public void minigames(PlayerW sender) {
		MinigameGUI.INVENTORY.open(sender.hook());
	}

	@Execute(route = "spawn")
	public void spawn(PlayerW sender) {
		var srx = Seriex.get();
		if (sender.playerState() == PlayerState.DUEL) {
			srx.msg(sender, Messages.COMMAND_TELEPORT_FAILURE_TELEPORTER_IN_DUEL);
			return;
		}

		Minigame currentMinigame = sender.currentMinigame();
		if (currentMinigame != null)
			currentMinigame.onLeave(sender);
		else {
			ServerConfig serverConfig = srx.fileManager().config(ServerConfig.class);
			sender.hook().teleport(serverConfig.getWorldSpawn());
		}
	}

	@Execute(route = "anticheat", aliases = { "ac", "anti-cheat" }, min = 0, max = 1)
	public void anticheat(PlayerW sender, @Opt Option<Anticheat> anticheat) {
		if (anticheat.isEmpty()) {
			AnticheatGUI.INVENTORY.open(sender.hook());
			return;
		}

		var realAC = anticheat.get();
		realAC.convert(sender);
		Seriex.get().msg(sender, Messages.ANTICHEAT_SWITCH, realAC.displayName);
	}
}
