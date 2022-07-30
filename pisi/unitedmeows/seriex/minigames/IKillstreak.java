package pisi.unitedmeows.seriex.minigames;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IKillstreak {
	void giveStreak(Player player, int kills, ItemStack... stack);
}
