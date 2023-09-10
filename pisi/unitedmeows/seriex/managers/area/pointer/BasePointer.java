package pisi.unitedmeows.seriex.managers.area.pointer;

import pisi.unitedmeows.seriex.managers.area.pointer.serialized.impl.ItemPointer;

/**
 * I presents the input type. <br>
 * O presents the output type. <br>
 * C presents the config type. <br>
 * <br>
 * For example, to save an item in {@link ItemPointer} <br>
 * I = Map< String , Object > <br>
 * O = ItemStack <br>
 * C = {@link com.electronwill.nightconfig.core.Config} <br>
 * <br>
 * The reason why {@link com.electronwill.nightconfig.core.Config} is the config output type,
 * to save a map in NightConfig
 * you need to use this class, its basically the same with a map.
 */
public abstract class BasePointer<I, O, C> {
	private I object;

	protected BasePointer(I serializedPtr) {
		this.object = serializedPtr;
	}

	public abstract PointerType type();

	@Override
	public abstract String toString();

	public abstract O data();

	public abstract C ptr2cfg();

	public I input() {
		return object;
	}
}
