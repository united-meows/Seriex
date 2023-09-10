package pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import pisi.unitedmeows.seriex.managers.area.pointer.BasePointer;
import pisi.unitedmeows.seriex.managers.area.pointer.PointerType;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.SerializedPointerData;

public class ItemPointer extends SerializedPointerData<ItemStack> {
	public ItemPointer(ItemStack pointerData) {
		this(pointerData.serialize());
	}

	public ItemPointer(Map<String, Object> serializedPtr) {
		super(serializedPtr);
	}

	@Override
	public PointerType type() {
		return PointerType.ITEM;
	}

	@Override
	public ItemStack data() {
		return ItemStack.deserialize(input());
	}
}
