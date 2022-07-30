package pisi.unitedmeows.seriex.minigames.impl.survival;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.minigames.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.Kit;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class Survival extends Minigame {
	private static final Kit KIT = new Kit(ItemBuilder.of(Material.DIAMOND_SWORD).enchantment(Enchantment.DAMAGE_ALL, 5).enchantment(Enchantment.DURABILITY, 2173).build(), new ItemStack[] {
		ItemBuilder.of(Material.DIAMOND_HELMET).enchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4).enchantment(Enchantment.DURABILITY, 3).build(), ItemBuilder.of(Material.DIAMOND_CHESTPLATE).build(),
		ItemBuilder.of(Material.DIAMOND_LEGGINGS).build(), ItemBuilder.of(Material.DIAMOND_BOOTS).build()
	}, ItemBuilder.of(Material.POTION).durability(8229).amount(16).build(), ItemBuilder.of(Material.GOLDEN_APPLE).amount(3).setUnbreakable(true).build());
	private final Map<Item, Long> itemTracker = new HashMap<>();

	public Survival() {
		Seriex.get().getServer().getScheduler().runTaskTimer(Seriex.get(), () -> {
			Iterator<Item> iterator = itemTracker.keySet().iterator();
			while (iterator.hasNext()) {
				Item item = iterator.next();
				long timeDropped = itemTracker.get(item);
				if (timeDropped + Duration.ofSeconds(15).toMillis() < System.currentTimeMillis()) {
					item.remove();
					iterator.remove();
				}
			}
		}, 20L, 20L);
	}

	@EventHandler
	public void onPlayerRightClickNPC(PlayerInteractAtEntityEvent event) {
		if (!isInGame(event.getPlayer())) return;
		Entity rightClicked = event.getRightClicked();
		if (rightClicked instanceof Player && "Right click to get the kit!".equals(rightClicked.getName())) {
			KIT.give(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
		if (!isInGame(event.getPlayer())) return;
		boolean check = event.getItem() != null && event.getItem().getType() != null && event.getItem().getType() == Material.GOLDEN_APPLE && event.getItem().getItemMeta() != null
					&& event.getItem().getItemMeta().spigot() != null && !event.getItem().getItemMeta().spigot().isUnbreakable();
		if (check) {
			ItemStack item = new ItemStack(Material.AIR);
			event.setItem(item);
			event.getPlayer().setItemInHand(item);
		}
	}

	@EventHandler
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		if (!isInGame(event.getPlayer())) return;
		Item itemDrop = event.getItemDrop();
		if (itemDrop == null) return;
		ItemStack itemStack = itemDrop.getItemStack();
		if (itemStack == null) return;
		final Material drop = itemStack.getType();
		if (drop == Material.GLASS_BOTTLE) {
			itemDrop.remove();
		} else {
			itemTracker.put(itemDrop, System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (!isInGame(player)) return;
			event.setFoodLevel(20);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!isInGame(player)) return;
		Player killer = player.getKiller();
		if (!isInGame(killer)) return;
		calculateKillstreak(killer);
		event.getDrops().clear();
		PlayerInventory inventory = player.getInventory();
		long currentTimeMillis = System.currentTimeMillis();
		Location killerLocation = killer.getLocation();
		Location playerLocation = player.getLocation();
		Location preferedLocation = playerLocation.distance(spawnLocation) < 5 ? killerLocation : playerLocation;
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack inventoryContents = contents[i];
			Item droppedItem = player.getWorld().dropItemNaturally(preferedLocation, inventoryContents);
			itemTracker.put(droppedItem, currentTimeMillis);
		}
		ItemStack[] armorContents = inventory.getArmorContents();
		for (int i = 0; i < armorContents.length; i++) {
			ItemStack inventoryContents = armorContents[i];
			Item droppedItem = player.getWorld().dropItemNaturally(preferedLocation, inventoryContents);
			itemTracker.put(droppedItem, currentTimeMillis);
		}
	}

	@Override
	public void onJoin(PlayerW playerW) {
		super.onJoin(playerW);
		KIT.give(playerW.getHooked());
	}

	@Override
	public void onLeave(PlayerW playerW) {
		super.onLeave(playerW);
	}
}
