package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class LanguageGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("language_gui")
				.provider(new LanguageGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Language selection")
				.build();


	@Override
	public void init(Player player, InventoryContents contents) {
		contents.setProperty("tail", new Tail());

		PlayerW user = Seriex.get().dataManager().user(player);
		Language[] values = Language.values();
		for (int i = 0; i < values.length; i++) {
			Language language = values[i];
			contents.set(1, 1 + i * 2, ClickableItem.of(
						ItemBuilder.head(language.headData()).name("&f" + language.name())
						.selected(() -> user.selectedLanguage() == language).build(),
						e -> {
							user.setLanguage(language);
							GuiSettings.refreshGUI(player, INVENTORY);
						}));
		}
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
