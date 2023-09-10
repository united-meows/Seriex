package pisi.unitedmeows.seriex.util.config.single.impl;

import java.util.ArrayList;
import java.util.List;

import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Maintainers")
public class MaintainersConfig extends SingleConfig {
	@ConfigField public ConfigValue<List<String>> MAINTAINERS = new ConfigValue<>("maintainers",
				Create.create(new ArrayList<>(), arraylist -> {
					arraylist.add("ghost2173");
				}));
}
