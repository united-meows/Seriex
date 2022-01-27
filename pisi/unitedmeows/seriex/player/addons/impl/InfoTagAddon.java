package pisi.unitedmeows.seriex.player.addons.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerMoveEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.player.PlayerW;
import pisi.unitedmeows.seriex.player.addons.PlayerAddon;
import pisi.unitedmeows.yystal.parallel.Async;

import java.util.ArrayList;
import java.util.List;

public class InfoTagAddon extends PlayerAddon {

	private List<InfoLine> lines = new ArrayList<>();

	public InfoTagAddon(PlayerW _playerW) {
		super(_playerW);
	}

	@Override
	public void onActivated() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Seriex._self, () -> add("hello world :D"), 40);
	}

	@Override
	public void onDisabled() {
		lines.forEach(InfoLine::remove);
		lines.clear();
	}

	public void add(String line) {
		InfoLine infoLine = new InfoLine(this);
		infoLine.createLine(line, playerW().getHooked().getWorld());
		lines.add(infoLine);
	}

	@Override
	public void onMove(PlayerMoveEvent event) {
		if (!event.isCancelled())
			lines.forEach(x->x.teleport(event.getTo().clone().add(0, 0.7, 0)));
	}

	protected static class InfoLine {
		private ArmorStand entity;
		private InfoTagAddon owner;

		public InfoLine(InfoTagAddon _owner) {
			owner = _owner;
		}

		public void createLine(String line, World world) {
			entity = world.spawn(owner.playerW().getHooked().getLocation(), ArmorStand.class);
			entity.setVisible(false);
			entity.setRemoveWhenFarAway(true);
			entity.setGravity(false);
			entity.setCustomNameVisible(true);

			entity.setCustomName(line);
		}

		public void remove() {
			entity.remove();
		}

		public void teleport(Location location) {
			if (entity != null) {
				entity.teleport(location);
			}
		}

		public void setLine(String line) {
			entity.setCustomName(line);
		}
	}
}
