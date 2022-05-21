package pisi.unitedmeows.seriex.managers.area.areas.base.impl.spawn;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import pisi.unitedmeows.seriex.managers.area.areas.base.SpawnArea;

public class MainSpawn extends SpawnArea {

	@EventHandler
	public void onFall(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (isInArea(player) && event.getCause() == FALL) {
				// check for falldamage setting and disable
			}
		}
	}
}
