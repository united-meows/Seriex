package pisi.unitedmeows.seriex.managers.area.areas;

import java.util.List;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.util.lists.GlueList;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.yystal.utils.CoID;

public class Area {
	public AxisBB axis;
	public CoID coID;
	public List<Player> playersInArea = new GlueList<>();

	public Area(AxisBB limits) {}

	public void enable() {}

	public void disable() {}

	public void enter(Player player) {}

	public void leave(Player player) {}
}
