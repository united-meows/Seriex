package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class AnticheatGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("anticheat_gui")
				.provider(new AnticheatGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Anti-cheat selection")
				.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerW user = Seriex.get().dataManager().user(player);
		
		contents.setProperty("tail", new Tail());

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.IRON_SWORD).name("&bN&bC&cP &d3.11.1")
					.selected(() -> Anticheat.NCP_MINEZ.equals(user.anticheat())).build(), e -> {
						Anticheat.NCP_MINEZ.convert(Seriex.get().dataManager().user(player));
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 3, ClickableItem.of(ItemBuilder.of(Material.DIAMOND_SWORD).name("&bN&bC&cP &d3.13.7")
					.selected(() -> Anticheat.NCP_FC.equals(user.anticheat())).build(), e -> {
						Anticheat.NCP_FC.convert(Seriex.get().dataManager().user(player));
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 5, ClickableItem.of(ItemBuilder.of(Material.WOOD_SWORD).name("&bN&bC&cP &d3.16.1")
					.lore("&cWarning &fThis is the fork of NCP called 'UpdatedNCP'")
					.selected(() -> Anticheat.NCP_LATEST.equals(user.anticheat())).build(), e -> {
						Anticheat.NCP_LATEST.convert(Seriex.get().dataManager().user(player));
						GuiSettings.refreshGUI(player, INVENTORY);
					}));
		contents.set(1, 7, ClickableItem.of(ItemBuilder.of(Material.GRASS).name("&2Van&ailla")
								.selected(() -> Anticheat.VANILLA.equals(user.anticheat())).build(), e -> {
			Anticheat.VANILLA.convert(Seriex.get().dataManager().user(player));
			GuiSettings.refreshGUI(player, INVENTORY);
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
