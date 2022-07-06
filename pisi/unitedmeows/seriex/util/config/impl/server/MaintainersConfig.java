package pisi.unitedmeows.seriex.util.config.impl.server;

import java.util.List;

import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;

@Cfg(name = "Maintainers" , manual = false , multi = false)
public class MaintainersConfig extends Config {
	private static List<String> defaultMaintainers = new GlueList<>();
	@ConfigField
	public ConfigValue<List<String>> MAINTAINERS = new ConfigValue<>(this, "maintainers", defaultMaintainers);

	public MaintainersConfig() {
		super("Maintainers");
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
