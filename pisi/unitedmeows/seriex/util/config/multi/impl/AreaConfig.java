package pisi.unitedmeows.seriex.util.config.multi.impl;

import pisi.unitedmeows.seriex.util.config.multi.MultiConfig;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

@Cfg(name = "Area")
public class AreaConfig extends MultiConfig {
	@ConfigField public ConfigValue<String> world_name = new ConfigValue<>("world_name", "");
	@ConfigField public ConfigValue<String> area_base = new ConfigValue<>("base", "");
	@ConfigField public ConfigValue<Integer> minX = new ConfigValue<>("min.x", 0);
	@ConfigField public ConfigValue<Integer> minY = new ConfigValue<>("min.y", 0);
	@ConfigField public ConfigValue<Integer> minZ = new ConfigValue<>("min.z", 0);
	@ConfigField public ConfigValue<Integer> maxX = new ConfigValue<>("max.x", 0);
	@ConfigField public ConfigValue<Integer> maxY = new ConfigValue<>("max.y", 0);
	@ConfigField public ConfigValue<Integer> maxZ = new ConfigValue<>("max.z", 0);
	@ConfigField public ConfigValue<Integer> warpX = new ConfigValue<>("warp.x", 0);
	@ConfigField public ConfigValue<Integer> warpY = new ConfigValue<>("warp.y", 0);
	@ConfigField public ConfigValue<Integer> warpZ = new ConfigValue<>("warp.z", 0);
}
