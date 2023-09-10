package pisi.unitedmeows.seriex.util;

import org.bukkit.plugin.java.JavaPlugin;

public interface PsuedoJavaPlugin {
	void onEnable(JavaPlugin plugin);

	void onDisable(JavaPlugin plugin);
}
