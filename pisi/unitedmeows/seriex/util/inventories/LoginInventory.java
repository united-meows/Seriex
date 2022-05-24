package pisi.unitedmeows.seriex.util.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import pisi.unitedmeows.seriex.Seriex;

public class LoginInventory implements InventoryProvider {
	public final SmartInventory INVENTORY = SmartInventory.builder().id("myInventory")
				.type(InventoryType.ANVIL).provider(new LoginInventory()).title("Login UI :D")
				.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		INVENTORY.setCloseable(false);
		ItemStack questionMark = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) questionMark.getItemMeta();
		meta.setOwner("MHF_Question");
		questionMark.setItemMeta(meta);
		contents.add(ClickableItem.of(
					ItemBuilder.of(questionMark).name("&dForgot your password?")
								.lore("Open a ticket on discord using the #ticket channel.").build(),
					(InventoryClickEvent event) -> Seriex.get().sendMessage(player, "Clicking does nothing...")));
		contents.add(ClickableItem.of(ItemBuilder.of(Material.BARRIER).build(),
					(InventoryClickEvent event) -> Seriex.get().sendMessage(player, "Clicking does nothing...")));
		contents.add(ClickableItem.of(ItemBuilder.of(Material.BOOK_AND_QUILL).name("Click to login!").build(),
					(InventoryClickEvent event) -> {
						Seriex.get().sendMessage(player, "Logging in...");
						// TODO
					}));
	}

	@Override
	public void update(Player arg0, InventoryContents arg1) {
		// TODO Auto-generated method stub
	}
}
