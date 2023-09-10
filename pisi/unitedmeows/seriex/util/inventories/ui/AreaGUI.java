package pisi.unitedmeows.seriex.util.inventories.ui;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.AreaManager;
import pisi.unitedmeows.seriex.managers.area.impl.Area.AreaCategory;
import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.wrapper.PlayerState;

public class AreaGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("area_gui")
				.provider(new AreaGUI())
				.size(4, 9	)
				.title(ChatColor.LIGHT_PURPLE + "Teleport to an area")
				.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		if (Seriex.get().dataManager().user(player).playerState() != PlayerState.SPAWN) {
			Seriex.get().msg(player, Messages.AREA_ONLY_IN_SPAWN);
			return;
		}

		contents.setProperty("tail", new Tail(8, 3));

		Pagination pagination = contents.pagination();
		AreaManager areaManager = Seriex.get().areaManager();
		ClickableItem[] items = Create.create(new ArrayList<ClickableItem>(), list -> {
			int[] index = { 1 };
			areaManager.areaList.forEach(area -> {
				Material categoryBasedItem = switch (area.category) {
					case COMBAT -> Material.DIAMOND_SWORD;
					case MISC -> Material.BOOK_AND_QUILL;
					case MOVEMENT -> Material.DIAMOND_BARDING;
					case PLAYER -> Material.SKULL;
				};

				// @DISABLE_FORMATTING
				// @ENABLE_FORMATTING

				list.add(ClickableItem.of(ItemBuilder.of(categoryBasedItem)
							.name("&f" + area.name)
							.durability(area.category == AreaCategory.PLAYER ? 3 : 0)
							.amount(index[0]++) // TODO what if >64 areas?
							.build(), onClick -> {
								Messages message = Messages.AREA_NOT_IMPLEMENTED_YET;
								if (area.isReallyConfigured()) {
									player.teleport(area.warpLocation);
									message = Messages.AREA_TELEPORTING;
								}
								Seriex.get().msg(player, message, area.name);
							}));
			});
		}).toArray(ClickableItem[]::new);

		pagination.setItems(items);
		pagination.setItemsPerPage(7);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1));

		contents.set(3, 3,
					ClickableItem.of(
								ItemBuilder.of(Material.WOOL).name("&cPrevious page").durability(14).build(),
								e -> INVENTORY.open(player, pagination.previous().getPage())));

		contents.set(3, 5,
					ClickableItem.of(
								ItemBuilder.of(Material.WOOL).name("&aNext page").durability(5).build(),
								e -> INVENTORY.open(player, pagination.next().getPage())));

	}

	@Override
	public void update(Player player, InventoryContents contents) {
		int state = contents.property("state", 0);
		contents.setProperty("state", state + 1);

		if (state % GuiSettings.TAIL_DELAY != 0)
			return;

		Tail tail = contents.property("tail");
		if (tail == null)
			return;

		tail.update(contents);
	}
}
