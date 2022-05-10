package pisi.unitedmeows.seriex.managers.area.impl;

import java.util.List;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.managers.area.util.IArea;
import pisi.unitedmeows.seriex.util.lists.GlueList;

public class Area implements IArea {
	public List<Player> playersInArea = new GlueList<>();

	@Override
	public void enable() {
		// TODO Auto-generated method stub
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
	}
}
