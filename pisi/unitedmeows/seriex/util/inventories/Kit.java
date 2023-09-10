package pisi.unitedmeows.seriex.util.inventories;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import pisi.unitedmeows.seriex.Seriex;

public class Kit {
	public final ItemStack heldItem;
	public final ItemStack[] armor;
	public final ItemStack[] rest;
	public final ItemStack fill;
	private List<PotionEffect> potions;

	/**
	 * Gives kit and gives the rest of the items.
	 */
	public static Kit createKit(ItemStack heldItem, ItemStack[] armor, ItemStack... rest) {
		return new Kit(heldItem, armor, rest);
	}

	/**
	 * Gives kit and fills the rest of the inventory.
	 */
	public static Kit createKitAndFill(ItemStack heldItem, ItemStack[] armor, ItemStack fill) {
		return new Kit(heldItem, armor, fill, true);
	}

	public Kit potionEffects(PotionEffect... potionEffects) {
		this.potions = Arrays.asList(potionEffects);
		return this;
	}

	private Kit(ItemStack heldItem, ItemStack[] armor, ItemStack... rest) {
		this.heldItem = heldItem;
		this.armor = armor;
		this.rest = rest;
		this.fill = null;
	}

	// we have an extra argument so constructors dont mix
	private Kit(ItemStack heldItem, ItemStack[] armor, ItemStack fill, boolean ignoreMe) {
		this.heldItem = heldItem;
		this.armor = armor;
		this.fill = fill;
		this.rest = null;
	}

	public Player giveIf(Player player, BooleanSupplier supplier) {
		if (supplier.getAsBoolean()) { return give(player); }
		return player;
	}

	public Player giveClearIf(Player player, BooleanSupplier supplier) {
		if (supplier.getAsBoolean()) { return clearAndGive(player); }
		return player;
	}

	public Player clearAndGive(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents((ItemStack[]) null);
		return give(player);
	}

	public Player give(Player player) {
		Seriex.get().runLater(() -> {
			if (heldItem != null) {
				player.setItemInHand(heldItem);
			}
			if (armor != null) {
				ItemStack helmet = armor[0];
				ItemStack chestplate = armor[1];
				ItemStack leggings = armor[2];
				ItemStack boots = armor[3];
				if (helmet != null) {
					player.getInventory().setHelmet(helmet);
				}
				if (chestplate != null) {
					player.getInventory().setChestplate(chestplate);
				}
				if (leggings != null) {
					player.getInventory().setLeggings(leggings);
				}
				if (boots != null) {
					player.getInventory().setBoots(boots);
				}
			}
			if (rest != null) {
				player.getInventory().addItem(rest);
			} else if (fill != null) {
				ItemStack[] contents = player.getInventory().getContents();
				for (ItemStack itemStack : contents) {
					if (itemStack == null) {
						player.getInventory().addItem(fill);
					}
				}
			}

			if (potions != null && !potions.isEmpty()) {
				potions.forEach(player::addPotionEffect);
			}
		}, 5);
		return player;
	}
}
