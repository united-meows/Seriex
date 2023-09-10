package pisi.unitedmeows.seriex.util.config.single.impl;

import java.util.ArrayList;
import java.util.List;

import io.netty.util.internal.ThreadLocalRandom;
import pisi.unitedmeows.seriex.util.Create;
import pisi.unitedmeows.seriex.util.config.single.SingleConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "MOTD")
public class MOTDConfig extends SingleConfig {
	@ConfigField private ConfigValue<List<String>> MOTDs = new ConfigValue<>("motd_list", Create.create(new ArrayList<>(), arrayList -> {
		arrayList.add("Now with more Seriex!");
		arrayList.add("Now with less Seriex!");
		arrayList.add("hmm today i will");
	})); 

	public String randomMOTD() {
		List<String> values = MOTDs.value();
		return values.get(ThreadLocalRandom.current().nextInt(values.size()));
	}
}
