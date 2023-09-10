package pisi.unitedmeows.seriex.util.inventories.ui;

import org.bukkit.entity.Player;

import fr.minuskube.inv.SmartInventory;
import pisi.unitedmeows.seriex.Seriex;

// TODO: cfg?
public class GuiSettings {
	public static final int DELAY = 2;
	public static final int TAIL_DELAY = 2;
	
	public static void refreshGUI(Player player, SmartInventory inv) {
		player.closeInventory();
		Seriex.get().runLater(() -> {
			inv.open(player);
		}, DELAY);
	}
}
