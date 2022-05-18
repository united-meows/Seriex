package pisi.unitedmeows.seriex.util;

import java.util.ArrayList;
import java.util.List;

public class MaintainersUtil {

	private static final List<String> maintainers = new ArrayList<>(4);
	static {
		maintainers.add("slowcheet4h");
		maintainers.add("ghost2173");
		maintainers.add("ipana2173");
		maintainers.add("SemoTeo");
	}

	public static boolean isMaintainer(String username) {
		return maintainers.contains(username);
	}
}
