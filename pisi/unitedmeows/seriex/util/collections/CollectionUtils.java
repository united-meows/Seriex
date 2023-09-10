package pisi.unitedmeows.seriex.util.collections;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {
	public static <T> List<T> getUncommonElements(List<T> first, List<T> second) {
		List<T> uncommon = new ArrayList<>();
		for (T t : first) {
			if (!second.contains(t)) {
				uncommon.add(t);
			}
		}
		for (T t : second) {
			if (!first.contains(t)) {
				uncommon.add(t);
			}
		}
		return uncommon;
	}
}
