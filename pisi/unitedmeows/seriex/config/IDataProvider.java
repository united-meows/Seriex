package pisi.unitedmeows.seriex.config;

import pisi.unitedmeows.seriex.config.impl.PlayerConfig;

public interface IDataProvider {

	/* gets player config from username */
	PlayerConfig playerConfig(String username);
}
