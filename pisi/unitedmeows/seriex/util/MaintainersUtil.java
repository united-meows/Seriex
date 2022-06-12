package pisi.unitedmeows.seriex.util;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.impl.server.MaintainersConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class MaintainersUtil extends Manager {
	private static List<String> maintainers;

	// We add maintainers in post just to be sure because sometimes
	// FileManager isnt being correctly initialized
	@Override
	public void post(Seriex seriex) {
		if (seriex == null) { // impossible
			System.err.println("how the fuck is seriex null");
			System.exit(1);
			return;
		}
		if (seriex.fileManager() == null) { // what
			seriex.logger().fatal("Seriex FileManager is null in MaintainersUtil#post!");
			return;
		}
		maintainers = new ArrayList<>();
		MaintainersConfig config = (MaintainersConfig) seriex.fileManager().getConfig(seriex.fileManager().MAINTAINERS);
		maintainers.addAll(config.MAINTAINERS.value());
		if (maintainers.size() > 10) {
			seriex.logger().info("How the fuck do we have more than 10 maintainers?");
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		maintainers = new ArrayList<>();
	}

	public static boolean isMaintainer(String username) {
		return maintainers.contains(username);
	}
}
