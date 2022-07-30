package pisi.unitedmeows.seriex.util.inventories;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Kit {
	public ItemStack heldItem;
	public ItemStack[] armor;
	public ItemStack[] rest;

	public Kit(ItemStack heldItem, ItemStack[] armor, ItemStack... rest) {
		this.heldItem = heldItem;
		this.armor = armor;
		this.rest = rest;
	}

	public Player give(Player player) {
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
		}
		return player;
	}
}
