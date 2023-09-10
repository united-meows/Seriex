package pisi.unitedmeows.seriex.managers.data;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.anticheat.Anticheat;

public class PlayerHistory {
	private final Map<ContentType, Map<Integer, ItemStack>> items = new EnumMap<>(ContentType.class);
	private final List<PotionEffect> effects;
	private final Anticheat anticheat;
	private final double health;
	private final int hunger;
	private Location location;

	public static PlayerHistory createHistory(Player player) {
		return createHistory(player, false);
	}

	public static PlayerHistory createHistory(Player player, boolean excludeInventory) {
		return new PlayerHistory(player, excludeInventory);
	}

	private PlayerHistory(List<PotionEffect> effects, double health, int hunger, Location location, Anticheat anticheat) {
		this.effects = effects;
		this.health = health;
		this.hunger = hunger;
		this.location = location;
		this.anticheat = anticheat;
	}

	private PlayerHistory(Player player, boolean excludeInventory) {
		this(Lists.newArrayList(player.getActivePotionEffects()),
					player.getHealth(),
					player.getFoodLevel(),
					player.getLocation().clone(),
					Seriex.get().dataManager().user(player).anticheat());
		
		if (excludeInventory)
			return;
		
		addToMap(player.getInventory(), this.items);
	}

	public void restore(Player player) {
		double maxHealth = player.getMaxHealth();
		player.addPotionEffects(this.effects);
		player.setHealth(Math.min(this.health, maxHealth));
		player.setFoodLevel(this.hunger);
		fillFromMap(player.getInventory(), this.items);
		player.updateInventory();
		anticheat.convert(Seriex.get().dataManager().user(player));
	}

	public static void fillFromMap(PlayerInventory inventory, Map<ContentType, Map<Integer, ItemStack>> items) {
		Map<Integer, ItemStack> inventoryItems = items.get(ContentType.INVENTORY);
		if (inventoryItems != null) {
			for (Map.Entry<Integer, ItemStack> entry : inventoryItems.entrySet())
				inventory.setItem(entry.getKey(), entry.getValue().clone());
		}
		Map<Integer, ItemStack> armorItems = items.get(ContentType.ARMOR);
		if (armorItems != null) {
			ItemStack[] armor = new ItemStack[4];
			armorItems.forEach((slot, item) -> armor[4 - slot.intValue()] = item.clone());
			inventory.setArmorContents(armor);
		}
	}

	public static void addToMap(PlayerInventory inventory, Map<ContentType, Map<Integer, ItemStack>> items) {
		Map<Integer, ItemStack> contents = new HashMap<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = inventory.getItem(i);
			if (item != null && item.getType() != Material.AIR)
				contents.put(i, item.clone());
		}
		items.put(ContentType.INVENTORY, contents);
		Map<Integer, ItemStack> armorContents = new HashMap<>();
		for (int j = inventory.getArmorContents().length - 1; j >= 0; j--) {
			ItemStack item = inventory.getArmorContents()[j];
			if (item != null && item.getType() != Material.AIR)
				armorContents.put(4 - j, inventory.getArmorContents()[j].clone());
		}
		items.put(ContentType.ARMOR, armorContents);
	}

	public enum ContentType {
		ARMOR,
		INVENTORY
	}
}
