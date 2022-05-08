package pisi.unitedmeows.seriex.managers.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

/**
 * @author ghost2173
 * 
 * @since May 4, 2022 3:28:11 PM
 * 
 * @apiNote Not thread-safe.
 */
public class DataManager implements ICleanup {
	// TODOL -> Investigate # maybe IdentityHashMap has better performance and for more stability?
	// Also we could use LinkedHashMap for removeEldestEntry to remove memory leaks maybe...
	// AND using Player as keys CAUSES MEMORY LEAKS. Certainly we could use UUIDs but that makes us calculate
	// the player every time we try to get Player from UUID. (see Bukkit.getPlayer(UUID))
	// I dont know maybe I am thinking too much about useless things...
	private final Map<Player, PlayerW> userMap = new HashMap<>();

	public PlayerW addUser(Player player) {
		Seriex.get().logger().info("Added %s to the database!", player.getName());
		return userMap.computeIfAbsent(player, PlayerW::new); // things slowcheet4h never learn
	}

	public void removeUser(Player player) {
		Seriex.get().logger().info("Removed %s from the database!", player.getName());
		userMap.remove(player);
	}

	/**
	 * @apiNote Dont use this if you dont have to, heavy operation.
	 *          <br>
	 *          (could be optimized using basic ass for loops but this looks cooler)
	 * 
	 */
	public void removeUser(PlayerW user) {
		Optional<Player> player = userMap.entrySet().stream().filter((Entry<Player, PlayerW> entry) -> entry.getValue() == user).map(Entry::getKey).findAny();
		if (player.isPresent()) {
			removeUser(player.get());
		}
	}

	@Override
	public void cleanup() {
		userMap.clear();
	}
}
