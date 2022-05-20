package pisi.unitedmeows.seriex.util.config.impl.server;

import static com.electronwill.nightconfig.core.CommentedConfig.*;
import static pisi.unitedmeows.seriex.util.config.impl.server.WorldConfig.WorldType.*;

import java.io.File;
import java.util.Arrays;

import org.bukkit.World;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.ConfigField;
import pisi.unitedmeows.seriex.util.config.impl.ConfigValue;
import pisi.unitedmeows.yystal.utils.Pair;

// TODO implement
public class WorldConfig extends Config {
	@ConfigField
	public ConfigValue<WorldType> WORLD_TPYE = new ConfigValue<>(this, "world.type", NULL);

	public WorldConfig(File toWrite, String extension, World... worlds) {
		super("WorldConfig", true, ConfigType.MULTIPLE, toWrite);
		Arrays.stream(worlds).forEach(world -> {
			File file = new File(String.format("%s/%s%s", toWrite, world.getName(), extension));
			configs.put(world.getName(), new Pair<>(file, inMemoryConcurrent()));
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

	public enum WorldType {
		SPAWN,
		ANARCHY, // for skywars & bedwars or for a world where everything is breakable unless we disallow it ourselves
		MINIGAME,
		NULL;
	}
}
