package pisi.unitedmeows.seriex.util.wrapper;

/**
 * This allows us to differentiate if the player is in some kind of activity, and alter the player accordingly. <br> <br>
 * Possible values:
 * <p>
 * <ul>
 * <li>{@link #DUEL}
 * <li>{@link #MINIGAMES}
 * <li>{@link #SPAWN}
 * </ul>
 * <p>
 */
public enum PlayerState {
	DUEL,
	MINIGAMES,
	SPAWN
}
