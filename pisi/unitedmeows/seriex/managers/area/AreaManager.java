package pisi.unitedmeows.seriex.managers.area;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.managers.area.areas.BasicArea;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.math.AxisBB;

public class AreaManager extends Manager implements Listener {
	public List<Area> areaList = new GlueList<>();

	@Override
	public void start(Seriex seriex) {
		areaList.forEach(Area::enable);
	}

	public BasicArea createArea(AxisBB limits) {
		final BasicArea basicArea = new BasicArea(limits);
		areaList.add(basicArea);
		return basicArea;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		for (int i = 0; i < areaList.size(); i++) {
			Area area = areaList.get(i);
			boolean prevInside = area.limits().isLocInside(event.getFrom());
			boolean nowInside = area.limits().isLocInside(event.getTo());
			Player player = event.getPlayer();
			if (!prevInside && nowInside) {
				area.enter(player);
				area.playersInArea().add(player);
			} else if (prevInside && !nowInside) {
				area.leave(player);
				area.playersInArea().remove(player);
			}
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		areaList.forEach(Area::disable);
	}
}
