package pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl;

import java.util.Map;

import org.bukkit.Location;

import pisi.unitedmeows.seriex.managers.area.pointer.PointerType;
import pisi.unitedmeows.seriex.managers.area.pointer.serialized.SerializedPointerData;

public class LocationPointer extends SerializedPointerData<Location> {
	public LocationPointer(Location pointerData) {
		this(pointerData.serialize());
	}

	public LocationPointer(Map<String, Object> serializedPtr) {
		super(serializedPtr);
	}

	@Override
	public PointerType type() {
		return PointerType.LOCATION;
	}

	@Override
	public Location data() {
		return Location.deserialize(input());
	}
}
