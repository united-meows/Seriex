package pisi.unitedmeows.seriex.managers.area.pointer.normal.impl;

import pisi.unitedmeows.seriex.managers.area.pointer.PointerType;
import pisi.unitedmeows.seriex.managers.area.pointer.normal.BasicPointer;

public class BooleanPointer extends BasicPointer<Boolean> {
	public BooleanPointer(Boolean serializedPtr) {
		super(serializedPtr);
	}

	@Override
	public PointerType type() {
		return PointerType.BOOLEAN;
	}
}
