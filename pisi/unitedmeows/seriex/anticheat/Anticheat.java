package pisi.unitedmeows.seriex.anticheat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;

/*
 * AAC,
 * OLDNCP,
 * NCP,
 * SPARRTAN,
 * SUBCAT,
 * THOTPATROL,
 * HORIZON,
 * ACR,
 * VANILLA,
 */
public class Anticheat {

	volatile String name;
	volatile String pluginName;
	List<String> dependantAnticheats = new CopyOnWriteArrayList<>();

	public Anticheat(final String name, final String pluginName, final String... depends) {
		this.name = name;
		this.pluginName = pluginName;
		dependantAnticheats.addAll(Arrays.asList(depends));
		Seriex.logger().info("Registered Anticheat %s", name);
		Seriex.get().antiCheats().add(this);
	}

	public void enableForPlayer(final Player player) {
		dependantAnticheats.forEach((String name) -> getAnticheat(name).ifPresent(ac -> ac.enableForPlayer(player)));
	}

	public void disableForPlayer(final Player player) {
		dependantAnticheats.forEach((String name) -> getAnticheat(name).ifPresent(ac -> ac.disableForPlayer(player)));
	}

	public static Optional<Anticheat> getAnticheat(final String name) {
		return Seriex.get().antiCheats().stream().filter(ac -> ac.name.equalsIgnoreCase(name)).findAny();
	}

	public static void switchTo(final Player player, final String name) {
		getAnticheat(name).ifPresent(found -> {
			Seriex.get().antiCheats().stream().filter(filter -> filter != found).forEach(anticheat -> anticheat.disableForPlayer(player));
			found.enableForPlayer(player);
		});
	}
}
