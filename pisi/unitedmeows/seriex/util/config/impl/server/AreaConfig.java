package pisi.unitedmeows.seriex.util.config.impl.server;

import static com.electronwill.nightconfig.core.CommentedConfig.*;
import static pisi.unitedmeows.seriex.util.config.impl.Config.ConfigType.*;

import java.io.File;
import java.util.Arrays;

import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.config.util.ConfigValue;
import pisi.unitedmeows.yystal.utils.Pair;

@Cfg(name = "Area" , manual = false , multi = true)
public class AreaConfig extends Config {
	@ConfigField
	public ConfigValue<String> area_name = new ConfigValue<>(this, "name", "");
	@ConfigField
	public ConfigValue<String> world_name = new ConfigValue<>(this, "world_name", "");
	@ConfigField
	public ConfigValue<String> area_base = new ConfigValue<>(this, "base", "");
	@ConfigField
	public ConfigValue<Area.Category> area_category = new ConfigValue<>(this, "category", Area.Category.NONE);
	@ConfigField
	public ConfigValue<Integer> minX = new ConfigValue<>(this, "min.x", 0);
	@ConfigField
	public ConfigValue<Integer> minY = new ConfigValue<>(this, "min.y", 0);
	@ConfigField
	public ConfigValue<Integer> minZ = new ConfigValue<>(this, "min.z", 0);
	@ConfigField
	public ConfigValue<Integer> maxX = new ConfigValue<>(this, "max.x", 0);
	@ConfigField
	public ConfigValue<Integer> maxY = new ConfigValue<>(this, "max.y", 0);
	@ConfigField
	public ConfigValue<Integer> maxZ = new ConfigValue<>(this, "max.z", 0);
	@ConfigField
	public ConfigValue<Integer> warpX = new ConfigValue<>(this, "warp.x", 0);
	@ConfigField
	public ConfigValue<Integer> warpY = new ConfigValue<>(this, "warp.y", 0);
	@ConfigField
	public ConfigValue<Integer> warpZ = new ConfigValue<>(this, "warp.z", 0);

	public AreaConfig(File toWrite, String extension, Area... areas) {
		super("Area", true, MULTIPLE, toWrite);
		Arrays.stream(areas).forEach(area -> {
			File file = new File(String.format("%s/%s%s", toWrite, area.name(), extension));
			configs.put(area.name(), new Pair<>(file, inMemoryConcurrent()));
		});
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
