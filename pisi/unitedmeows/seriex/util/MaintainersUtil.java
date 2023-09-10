package pisi.unitedmeows.seriex.util;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.single.impl.MaintainersConfig;

public class MaintainersUtil extends Manager {
	private static final List<String> MAINTAINERS = new ArrayList<>();

	@Override
	public void post(Seriex seriex) {
		MaintainersConfig config = seriex.fileManager().config(MaintainersConfig.class);
		MAINTAINERS.addAll(config.MAINTAINERS.value());
		if (MAINTAINERS.size() > 10) {
			seriex.logger().info("How the fuck do we have more than 10 maintainers?");
		}
	}

	public static boolean isMaintainer(String username) {
		return MAINTAINERS.contains(username);
	}
}
