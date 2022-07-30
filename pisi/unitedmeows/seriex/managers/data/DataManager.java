package pisi.unitedmeows.seriex.managers.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

// should be threadsafe
public class DataManager extends Manager {
	private Map<Player, PlayerW> userMap;

	@Override
	public void start(Seriex seriex) {
		// note for self:
		// opening a new instead of .clear should use less memory because it allocates a totally new object
		userMap = new ConcurrentHashMap<>(); // reload & restart
		seriex.get().getServer().getOnlinePlayers().forEach(this::user);
	}

	public PlayerW user(Player player) {
		return userMap.computeIfAbsent(player, computedPlayer -> {
			Seriex.logger().info("Added %s to the temporary database!", player.getName());
			return new PlayerW(computedPlayer);
		});
	}

	public void removeUser(Player player) {
		Seriex.logger().info("Removed %s from the database!", player.getName());
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
		player.ifPresent(this::removeUser);
	}

	@Override
	public void cleanup() {
		userMap.clear(); // clear is good enough here jvm should handle the rest
	}
}
