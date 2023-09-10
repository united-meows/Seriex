package pisi.unitedmeows.seriex.managers.minigames.killstreak.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.managers.minigames.killstreak.IKillstreak;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;

public class PotPVPGappleStreak implements IKillstreak {

	@Override
	public boolean giveStreak(Minigame minigame, Player player, int kills) {
		if (kills % 5 == 0) {
			giveItem(player, Material.POTION, ItemBuilder.of(Material.GOLDEN_APPLE).amount(3).unbreakable(true).build());
			return true;
		}
		return false;
	}

	/**
	 * Adds the item to the first empty inventory,
	 *  if there isnt any replaces the first item with the material type defined in the arguments
	 */
	void giveItem(Player player, Material type, ItemStack itemStack) {
		ItemStack[] contents = player.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack inventoryStack = contents[i];
			if (inventoryStack == null || inventoryStack.getType() == type || inventoryStack.getType() == Material.AIR) {
				player.getInventory().setItem(i, itemStack);
				break;
			}
		}
	}
}
