package pisi.unitedmeows.seriex.managers.minigames.killstreak.impl;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;
import pisi.unitedmeows.seriex.managers.minigames.killstreak.IKillstreak;

public class BasicStreak implements IKillstreak {
	@Override
	public boolean giveStreak(Minigame minigame, Player player, int kills) {
		return kills == 3 || kills % 5 == 0;
	}
}
