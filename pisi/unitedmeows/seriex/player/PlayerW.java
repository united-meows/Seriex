package pisi.unitedmeows.seriex.player;

import org.bukkit.entity.Player;
import pisi.unitedmeows.yystal.clazz.HookClass;

public class PlayerW extends HookClass<Player> {

	public PlayerW(Player _player) {
		hooked = _player;
	}

}
