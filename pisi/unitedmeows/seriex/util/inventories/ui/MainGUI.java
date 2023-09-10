package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;

public class MainGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("main_gui")
				.provider(new MainGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Seriex Main GUI")
				.build();


	@Override
	public void init(Player player, InventoryContents contents) {
		contents.setProperty("tail", new Tail());

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.BOOK).name("&bPlayer Info").build(), e -> {
			GuiSettings.refreshGUI(player, PlayerInfoGUI.INVENTORY);
		}));
		contents.set(1, 3, ClickableItem.of(ItemBuilder.of(Material.DIAMOND_SWORD).name("&dMinigames").build(), e -> {
			GuiSettings.refreshGUI(player, MinigameGUI.INVENTORY);
		}));
		contents.set(1, 4, ClickableItem.of(ItemBuilder.of(Material.NETHER_STAR).name("&fSettings").build(), e -> {
			GuiSettings.refreshGUI(player, SettingsGUI.INVENTORY);
		}));
		contents.set(1, 5, ClickableItem.of(ItemBuilder.of(Material.EMERALD).name("&aWallet").build(), e -> {
			GuiSettings.refreshGUI(player, WalletGUI.INVENTORY);
		}));
		contents.set(1, 7, ClickableItem.of(ItemBuilder.of(Material.COMPASS).name("&dAreas").build(), e -> {
			GuiSettings.refreshGUI(player, AreaGUI.INVENTORY);
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
