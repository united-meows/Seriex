package pisi.unitedmeows.seriex;

import org.bukkit.plugin.java.JavaPlugin;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class SpigotPlugin extends JavaPlugin {
	private static final Seriex SERIEX = new Seriex();

	@Override
	public void onEnable() {
		SERIEX.onEnable(this);
	}

	@Override
	public void onDisable() {
		SERIEX.onDisable(this);
	}
}
