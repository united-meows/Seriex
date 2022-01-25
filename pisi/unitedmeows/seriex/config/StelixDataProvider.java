package pisi.unitedmeows.seriex.config;

import pisi.unitedmeows.seriex.config.impl.PlayerConfig;

public class StelixDataProvider implements IDataProvider
{
	@Override
	public PlayerConfig playerConfig(String username) {
		return null;
	}

	@Override
	public PlayerConfig playerConfigFromToken(String token) {
		return null;
	}

	@Override
	public PlayerConfig playerConfig(int id) {
		return null;
	}
}
