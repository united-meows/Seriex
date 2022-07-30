package pisi.unitedmeows.seriex.managers.area.areas.impl.movement;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import com.electronwill.nightconfig.core.CommentedConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.area.areas.base.BasicArea;
import pisi.unitedmeows.seriex.managers.area.areas.util.ImplementArea;
import pisi.unitedmeows.seriex.util.config.impl.server.AreaConfig;

@ImplementArea(name = "scaffold")
public class ScaffoldArea extends BasicArea {
	// called using reflections
	public ScaffoldArea(AreaConfig areaConfig, CommentedConfig commentedConfig, Category category) {
		super(areaConfig, commentedConfig, category);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (isInArea(player)) {
			Seriex.get().msg(player, "you are in scaffold area :DDD");
		}
	}
}
