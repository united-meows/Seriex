package pisi.unitedmeows.seriex.managers.area;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.lists.GlueList;

public class AreaManager extends Manager implements Listener {
	public List<Area> areaList = new GlueList<>();

	@Override
	public void start(Seriex seriex) {
		areaList.forEach(Area::enable);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		for (int i = 0; i < areaList.size(); i++) {
			Area area = areaList.get(i);
			boolean prevInside = area.axis.isLocInside(event.getFrom());
			boolean nowInside = area.axis.isLocInside(event.getTo());
			Player player = event.getPlayer();
			if (!prevInside && nowInside) {
				area.enter(player);
			} else if (prevInside && !nowInside) {
				area.leave(player);
			}
			if (nowInside) {
				area.playersInArea.add(player);
			}
		}
	}

	@Override
	public void cleanup() throws SeriexException {
		areaList.forEach(Area::disable);
	}
}
