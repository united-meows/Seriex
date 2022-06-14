package pisi.unitedmeows.seriex.auth.adapters;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import pisi.unitedmeows.seriex.Seriex;

public class InventoryPacketAdapter extends PacketAdapter {
	public InventoryPacketAdapter() {
		super(Seriex.get(), PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		byte windowID = event.getPacket().getIntegers().read(0).byteValue();
		if (windowID == 0 && Seriex.get().authentication().waitingForLogin(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	public void sendBlankInventoryPacket(Player player) {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		PacketContainer inventoryPacket = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
		inventoryPacket.getIntegers().write(0, 0);
		int inventorySize = 45;
		ItemStack[] blankInventory = new ItemStack[inventorySize];
		Arrays.fill(blankInventory, new ItemStack(Material.AIR));
		StructureModifier<ItemStack[]> itemArrayModifier = inventoryPacket.getItemArrayModifier();
		if (itemArrayModifier.size() > 0) {
			itemArrayModifier.write(0, blankInventory);
		} else {
			StructureModifier<List<ItemStack>> itemListModifier = inventoryPacket.getItemListModifier();
			itemListModifier.write(0, Arrays.asList(blankInventory));
		}
		try {
			protocolManager.sendServerPacket(player, inventoryPacket, false);
		}
		catch (InvocationTargetException invocationExc) {
			invocationExc.printStackTrace();
			Seriex.get().logger().fatal("Couldnt send blank inventory packet!");
		}
	}
}
