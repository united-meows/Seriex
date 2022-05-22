package pisi.unitedmeows.seriex.managers.area.areas;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.math.AxisBB;

public class BasicArea extends Area {
	private Consumer<Player> onEnter;
	private Consumer<Player> onLeave;
	private Consumer<BasicArea> tick;
	private BasicArea instance;
	private int runnableId = -1;

	public BasicArea(AxisBB limits) {
		super(limits, null);
		instance = this;
	}

	public BasicArea onEnter(Consumer<Player> _onEnter) {
		onEnter = _onEnter;
		return this;
	}

	public BasicArea onLeave(Consumer<Player> _onLeave) {
		onLeave = _onLeave;
		return this;
	}

	public BasicArea tick(Consumer<BasicArea> _tick, int interval) {
		if (interval > 0) {
			tick = _tick;
			runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Seriex.get(), () -> tick.accept(instance), interval, interval);
		}
		return this;
	}

	@Override
	public void enter(Player player) {
		if (onEnter != null) {
			onEnter.accept(player);
		}
		super.enter(player);
	}

	@Override
	public void leave(Player player) {
		if (onLeave != null) {
			onLeave.accept(player);
		}
		super.leave(player);
	}

	@Override
	public void disable() {
		if (runnableId != -1) {
			Bukkit.getServer().getScheduler().cancelTask(runnableId);
		}
		super.disable();
	}
}
