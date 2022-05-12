package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;
import java.util.List;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;
import pisi.unitedmeows.seriex.util.lists.GlueList;

public class MaintainersConfig extends Config {
	private static List<String> defaultMaintainers = new GlueList<>();
	@ConfigField
	public ConfigValue MAINTAINERS = new ConfigValue(this, "maintainers", defaultMaintainers);

	public MaintainersConfig(File toWrite) {
		super("Maintainers");
		this.toWrite = toWrite;
		defaultMaintainers.add("ghost2173");
		defaultMaintainers.add("ipana2173");
		defaultMaintainers.add("slowcheet4h");
		defaultMaintainers.add("SemoTeo");
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
