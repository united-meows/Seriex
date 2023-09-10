package pisi.unitedmeows.seriex.managers.area.pointer.normal;

import pisi.unitedmeows.seriex.managers.area.pointer.BasePointer;

public abstract class BasicPointer<X> extends BasePointer<X, X, X> {
	protected BasicPointer(X serializedPtr) {
		super(serializedPtr);
	}

	@Override
	public String toString() {
		return input().toString();
	}

	@Override
	public X data() {
		return input();
	}

	@Override
	public X ptr2cfg() {
		return input();
	}
}
