package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class MinigameGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("minigame_gui")
				.provider(new MinigameGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Minigame selection")
				.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.setProperty("tail", new Tail());

		PlayerW user = Seriex.get().dataManager().user(player);
		if (user.playerState() == PlayerState.DUEL) {
			Seriex.get().msg(user.hook(), Messages.MINIGAME_CANT_SWITCH_IN_DUEL);
			return;
		}
		
		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.IRON_SWORD).name("&bKonya&6craft").build(), e -> {
			Seriex.get().minigameManager().minigameMap.get("BabaPvP").onJoin(Seriex.get().dataManager().user(player));
			player.closeInventory();
		}));
		contents.set(1, 3, ClickableItem.of(ItemBuilder.of(Material.DIAMOND_SWORD).name("&4Surv&cival").build(), e -> {
			Seriex.get().minigameManager().minigameMap.get("Survival").onJoin(Seriex.get().dataManager().user(player));
			player.closeInventory();
		}));
		contents.set(1, 5, ClickableItem.of(ItemBuilder.of(Material.WOOD_SWORD).name("&aKit&fPVP").build(), e -> {
			Seriex.get().minigameManager().minigameMap.get("PotPVP").onJoin(Seriex.get().dataManager().user(player));
			player.closeInventory();
		}));
		contents.set(1, 7, ClickableItem.of(ItemBuilder.of(Material.BARRIER).name("Go to spawn").build(), e -> {
			PlayerW currentUser = Seriex.get().dataManager().user(player);
			Minigame currentMinigame = currentUser.currentMinigame();
			if (currentMinigame != null) {
				currentMinigame.onLeave(currentUser); // me when user.onLeave(user): 🤯🤯🤯🤯
				player.closeInventory();
			}
		}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		int state = contents.property("state", 0);
		contents.setProperty("state", state + 1);

		if (state % GuiSettings.TAIL_DELAY != 0)
			return;

		Tail tail = contents.property("tail");
		tail.update(contents);
	}
}
