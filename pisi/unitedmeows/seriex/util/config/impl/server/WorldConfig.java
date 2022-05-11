package pisi.unitedmeows.seriex.util.config.impl.server;

import static java.lang.String.*;
import static pisi.unitedmeows.seriex.util.config.impl.server.WorldConfig.WorldType.*;

import java.io.File;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;

// TODO implement
public class WorldConfig extends Config {
	@ConfigField
	private ConfigValue WORLD_TPYE = new ConfigValue(this, "world.type", NULL);

	public WorldConfig(String worldName, String extension, File toWrite) {
		super(format("WorldConfig_%s", worldName));
		this.toWrite = new File(toWrite, format("worlds/%s%s", worldName, extension));
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}

	public enum WorldType {
		SPAWN,
		ANARCHY, // for skywars & bedwars or for a world where everything is breakable unless we disallow it ourselves
		MINIGAME,
		NULL;
	}
}
