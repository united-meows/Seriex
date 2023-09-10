package pisi.unitedmeows.seriex.managers.minigames.killstreak;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.managers.minigames.impl.Minigame;

public interface IKillstreak {
	boolean giveStreak(Minigame minigame, Player player, int kills);
}
