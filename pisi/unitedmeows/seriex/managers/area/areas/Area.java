package pisi.unitedmeows.seriex.managers.area.areas;

import java.util.List;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.math.AxisBB;

public class Area {
	private AxisBB limits;
	private Area parentArea;
	private List<Player> playersInArea = new GlueList<>();

	public Area() {}

	public Area(AxisBB limits, Area parentArea) {
		this.limits = limits;
		this.parentArea = parentArea;
	}

	public void enable() {}

	public void disable() {}

	public void enter(Player player) {}

	public void leave(Player player) {}

	public AxisBB limits() {
		return limits;
	}

	public Area parentArea() {
		return parentArea;
	}

	public Area limits(AxisBB set) {
		this.limits = set;
		return this;
	}

	public Area parent(Area set) {
		this.parentArea = set;
		return this;
	}

	public List<Player> playersInArea() {
		return playersInArea;
	}

	public boolean isInArea(Player player) {
		return playersInArea.contains(player);
	}
}
