package pisi.unitedmeows.seriex.managers.virtualplayers.modifier.impl;

import static com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot.*;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

import dev.brighten.db.utils.TriConsumer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.modifier.VirtualModifier;

public class EquipmentModifier extends VirtualModifier {

	public EquipmentModifier(VirtualPlayer npc) {
		super(npc);
	}

	public EquipmentModifier queue(ItemSlot itemSlot, ItemStack equipment) {
		super.queue((npc, target) -> {
			int slotId = itemSlot.ordinal();
			if (slotId > 0) {
				// EnumWrappers.ItemSlot has offhand so we shift down by 1 to avoid offhand
				slotId--;
			}
			return new PacketPlayOutEntityEquipment(npc.entityID(), slotId, CraftItemStack.asNMSCopy(equipment));
		});
		return this;
	}

	private final TriConsumer<VirtualPlayer, ItemSlot, ItemStack> equipConsumer = (player, slot, stack) -> {
		equip_bukkit: {
			Player bukkitPlayer = player.bukkitPlayer();
			EntityEquipment inventory = bukkitPlayer.getEquipment();
			switch (slot) {
				// @DISABLE_FORMATTING
				case HEAD: inventory.setHelmet(player.armor(stack, slot)); break;
				case CHEST: inventory.setChestplate(player.armor(stack, slot)); break;
				case LEGS: inventory.setLeggings(player.armor(stack, slot)); break;
				case FEET: inventory.setBoots(player.armor(stack, slot)); break;
				case MAINHAND: bukkitPlayer.setItemInHand(player.heldItem(stack)); break;
				default: break;
				// @ENABLE_FORMATTING
			}
			bukkitPlayer.updateInventory();
		}
	};

	public EquipmentModifier equip(ItemSlot itemSlot, ItemStack equipment) {
		this.equipConsumer.accept(npc, itemSlot, equipment);
		return this;
	}

	public EquipmentModifier update() {
		Player bukkitPlayer = npc.bukkitPlayer();
		EntityEquipment equipment = bukkitPlayer.getEquipment();

		this.queue(MAINHAND, bukkitPlayer.getItemInHand());

		this.queue(HEAD, equipment.getHelmet());
		this.queue(CHEST, equipment.getChestplate());
		this.queue(LEGS, equipment.getLeggings());
		this.queue(FEET, equipment.getBoots());
		return this;
	}
}
