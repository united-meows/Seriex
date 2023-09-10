package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class SettingsGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("settings_gui")
				.provider(new SettingsGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Settings GUI")
				.build();


	private String coloredBoolean(String name, boolean b) {
		return b ? "&a" + name : "&c" + name;
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		contents.setProperty("tail", new Tail());

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.BOOK).name("&fAnticheat").build(), e -> {
			GuiSettings.refreshGUI(player, AnticheatGUI.INVENTORY);
		}));
		contents.set(1, 3, ClickableItem.of(ItemBuilder.of(Material.COOKED_BEEF)
					.name(coloredBoolean("Hunger", Seriex.get().dataManager().user(player).playerSettings().hunger))
					.lore("&cWarning &fOnly works on spawn!")
					.selected(() -> Seriex.get().dataManager().user(player).playerSettings().hunger).build(), e -> {
						PlayerW user = Seriex.get().dataManager().user(player);
						StructPlayerSettings playerSettings = user.playerSettings();
						playerSettings.hunger ^= true;
						playerSettings.update();
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 4, ClickableItem.of(ItemBuilder.of(Material.DIAMOND_BOOTS)
					.name(coloredBoolean("Fall damage", Seriex.get().dataManager().user(player).playerSettings().fall_damage))
					.lore("&cWarning &fOnly works on spawn!")
					.selected(() -> Seriex.get().dataManager().user(player).playerSettings().fall_damage)
					.build(), e -> {
						PlayerW user = Seriex.get().dataManager().user(player);
						StructPlayerSettings playerSettings = user.playerSettings();
						playerSettings.fall_damage ^= true;
						playerSettings.update();
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 5, ClickableItem.of(ItemBuilder.of(Material.PAPER)
					.name(coloredBoolean("Show flags", Seriex.get().dataManager().user(player).playerSettings().flags))
					.selected(() -> Seriex.get().dataManager().user(player).playerSettings().flags).build(), e -> {
						PlayerW user = Seriex.get().dataManager().user(player);
						StructPlayerSettings playerSettings = user.playerSettings();
						playerSettings.flags ^= true;
						playerSettings.update();
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 7, ClickableItem.of(ItemBuilder.of(Material.BOOKSHELF).name("&fLanguage").build(), e -> {
			GuiSettings.refreshGUI(player, LanguageGUI.INVENTORY);
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
