package pisi.unitedmeows.seriex.util.config.multi.impl;

import static pisi.unitedmeows.seriex.util.config.multi.impl.WorldConfig.WorldType.UNDEFINED;

import org.bukkit.World;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.config.multi.MultiConfig;
import pisi.unitedmeows.seriex.util.config.multi.util.ConfigHandler;
import pisi.unitedmeows.seriex.util.config.multi.util.MultiConfigHandler;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;

// TODO implement
@Cfg(name = "World")
public class WorldConfig extends MultiConfig {
	@ConfigField public ConfigValue<WorldType> WORLD_TPYE = new ConfigValue<>("world.type", UNDEFINED);
	@ConfigHandler(start = true) public MultiConfigHandler handler = () -> Seriex.get().plugin().getServer().getWorlds().stream().map(World::getName).toList();

	public enum WorldType {
		SPAWN,
		MINIGAME,
		UNDEFINED;
	}
}
