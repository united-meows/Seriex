package pisi.unitedmeows.seriex.managers.data;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;

// should be threadsafe
public class DataManager extends Manager {
	private Map<Player, PlayerW> userMap;

	@Override
	public void start(Seriex seriex) {
		userMap = new ConcurrentHashMap<>(); // reload & restart
		seriex.get().plugin().getServer().getOnlinePlayers().forEach(this::user);
	}

	public PlayerW user(Player player) {
		return this.user(player, true);
	}

	public PlayerW user(Player player, boolean computeIfAbsent) {
        if (!player.isOnline()) {
            if (userMap.containsKey(player)) {
                removeUser(player);
            }
            throw SeriexException.create("Player is not online");
        }
        if (computeIfAbsent) {
            return userMap.computeIfAbsent(player, computedPlayer -> {
                Seriex.get().logger().info("Added {} to the temporary database!", player.getName());
                return new PlayerW(computedPlayer).init();
            });
        } else {
            return userMap.get(player);
        }
    }
	public void removeUser(Player player) {
		Seriex.get().logger().info("Removed {} from the database!", player.getName());
		userMap.remove(player);
	}

	/**
	 * @apiNote Dont use this if you dont have to, heavy operation.
	 *          <br>
	 *          (could be optimized using basic ass for loops but this looks cooler)
	 */
	public void removeUser(PlayerW user) {
		Optional<Player> player = userMap.entrySet().stream().filter((Entry<Player, PlayerW> entry) -> entry.getValue() == user).map(Entry::getKey).findFirst();
		player.ifPresent(this::removeUser);
	}

	@Override
	public void cleanup() {
		users().forEach(PlayerW::destruct);
		userMap.clear(); // clear is good enough here jvm should handle the rest
	}

	public Collection<PlayerW> users() {
		return userMap.values();
	}
}
