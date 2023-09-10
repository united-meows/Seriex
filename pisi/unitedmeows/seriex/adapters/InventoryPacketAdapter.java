package pisi.unitedmeows.seriex.adapters;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.Create;

public class InventoryPacketAdapter extends PacketAdapter {
	private static final int INVENTORY_WINDOW_ID = 0;

	public InventoryPacketAdapter() {
		super(Seriex.get().plugin(), PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		byte windowID = event.getPacket().getIntegers().read(0).byteValue();
		if (windowID == INVENTORY_WINDOW_ID && Seriex.get().authentication().waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	public void sendBlankInventoryPacket(Player player) {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

		final ItemStack[] blankInventory = Create.create(() -> {
			int inventorySize = 45;
			var arrayToFill = new ItemStack[inventorySize];
			Arrays.fill(arrayToFill, new ItemStack(Material.AIR));
			return arrayToFill;
		});

		// create packet
		PacketContainer inventoryPacket = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
		inventoryPacket.getIntegers().write(0, INVENTORY_WINDOW_ID);
		inventoryPacket.getItemArrayModifier().write(0, blankInventory);

		protocolManager.sendServerPacket(player, inventoryPacket, false);
	}
}
