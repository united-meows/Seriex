package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.inventories.ItemBuilder;
import pisi.unitedmeows.seriex.util.inventories.animation.Tail;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

public class WalletGUI implements InventoryProvider {

	public static final SmartInventory INVENTORY = SmartInventory.builder()
				.id("wallet_gui")
				.provider(new WalletGUI())
				.size(3, 9)
				.title(ChatColor.LIGHT_PURPLE + "Wallet GUI")
				.build();


	@Override
	public void init(Player player, InventoryContents contents) {
		contents.setProperty("tail", new Tail());

		PlayerW user = Seriex.get().dataManager().user(player);

		contents.set(1, 1, ClickableItem.of(ItemBuilder.of(Material.GOLD_INGOT).name("&fPawcoins in wallet")
					.lore(EnumChatFormat.LIGHT_PURPLE + String.valueOf(user.playerWallet().coins))
					.build(), e -> {
						GuiSettings.refreshGUI(player, AnticheatGUI.INVENTORY);
					}));
		contents.set(1, 4, ClickableItem.of(ItemBuilder.of(Material.BOOK).name("&fWallet")
					.lore(EnumChatFormat.LIGHT_PURPLE + user.playerWallet().player_wallet)
					.build(), e -> {
						Seriex.get().msg_click_on_copy(player, user.playerWallet().player_wallet, "Wallet ID");
					}));
		contents.set(1, 7, ClickableItem.empty(
					ItemBuilder.of(Material.PAPER)
								.name("&fSend pawcoins")
								.lore("&7/pay <player> amount")
								.build()));
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
