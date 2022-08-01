package pisi.unitedmeows.seriex.minigames.killstreak;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.minigames.IKillstreak;

public class BasicKillstreak implements IKillstreak {
	@Override
	public void giveStreak(Player player, int kills, ItemStack... stack) {
		if (kills == 3 || kills % 5 == 0) {
			if (stack == null || stack.length != 0) {
				player.getInventory().addItem(stack);
			}
			// msg
			Seriex.get().getServer().getOnlinePlayers().forEach(onlinePlayer -> {
				String translationMessage = Seriex.get().I18n().getString("minigame.winstreak", Seriex.get().dataManager().user(onlinePlayer));
				// TODO set message ("minigame.winstreak") in TranslationConfig
				// default message -> %s has gotten a %s killstreak
				Seriex.get().msg(player, translationMessage, player.getName(), kills);
			});
		}
	}
}
