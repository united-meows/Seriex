package pisi.unitedmeows.seriex.managers.area.areas;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.electronwill.nightconfig.core.CommentedConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.AreaConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.math.AxisBB;

// TODO getter && setter
public class Area implements Listener {
	private AxisBB limits;
	private Location warpLoc;
	private String areaName;
	private List<Player> playersInArea = new GlueList<>();
	private AreaConfig areaConfig;
	private CommentedConfig realConfig;
	private Category category;

	public Area(AreaConfig areaConfig, CommentedConfig real, Category category) {
		this.areaConfig = areaConfig;
		this.realConfig = real;
		areaName = areaConfig.area_name.value(real);
		limits = new AxisBB(areaConfig.world_name.value(real), areaConfig.minX.value(real), areaConfig.minY.value(real), areaConfig.minZ.value(real), areaConfig.maxX.value(real),
					areaConfig.maxY.value(real), areaConfig.maxZ.value(real));
		category = areaConfig.area_category.value(category, real).value();
		FileManager fileManager = Seriex.get().fileManager();
		ServerConfig config = (ServerConfig) fileManager.getConfig(fileManager.SERVER);
		warpLoc = new Location(Bukkit.getWorld(config.WORLD_NAME.value()), areaConfig.warpX.value(real), areaConfig.warpY.value(real), areaConfig.warpZ.value(real));
	}

	public void enable() {}

	public void disable() {}

	public void enter(Player player) {}

	public void leave(Player player) {}

	public AxisBB limits() {
		return limits;
	}

	public Area limits(AxisBB set) {
		this.limits = set;
		return this;
	}

	public List<Player> playersInArea() {
		return playersInArea;
	}

	public boolean isInArea(Player player) {
		return playersInArea.contains(player);
	}

	public String name() {
		return areaName;
	}

	public enum Category {
		COMBAT,
		MOVEMENT,
		PLAYER,
		MISC,
		NONE,
	}
}
