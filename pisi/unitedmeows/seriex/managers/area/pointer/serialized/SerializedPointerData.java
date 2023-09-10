package pisi.unitedmeows.seriex.managers.area.pointer.serialized;

import java.util.Arrays;
import java.util.Map;

import com.electronwill.nightconfig.core.Config;

import pisi.unitedmeows.seriex.managers.area.pointer.BasePointer;

public abstract class SerializedPointerData<X> extends BasePointer<Map<String, Object>, X, Config> {

	protected SerializedPointerData(Map<String, Object> serializedPtr) {
		super(serializedPtr);
	}

	@Override
	public Config ptr2cfg() {
		Config cfg = Config.inMemory();
		input().forEach(cfg::add);
		return cfg;
	}

	@Override
	public String toString() {
		return Arrays.toString(input().entrySet().toArray());
	}
}
