package pisi.unitedmeows.seriex.player.addons;

import org.bukkit.event.player.PlayerMoveEvent;
import pisi.unitedmeows.seriex.player.PlayerW;

public abstract class PlayerAddon {

	private PlayerW playerW;

	public void onActivated() { }
	public void onDisabled() { }

	public PlayerAddon(PlayerW _playerW) {
		playerW = _playerW;
	}

	public PlayerW playerW() {
		return playerW;
	}

	public void onMove(PlayerMoveEvent event) { }

}
