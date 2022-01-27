package pisi.unitedmeows.seriex.player;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.seriex.player.addons.PlayerAddon;
import pisi.unitedmeows.seriex.player.addons.impl.InfoTagAddon;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.utils.CoID;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;

public class PlayerW extends HookClass<Player> {

	private PlayerConfig playerConfig;
	private HashMap<Class<? extends PlayerAddon>, PlayerAddon> addons;

	public PlayerW(Player _player) {
		hooked = _player;

		/* addons */
		addons = new HashMap<>();
		addAddon(new InfoTagAddon(this));

		/* tries to retrieve the player config */
		playerConfig = Seriex._self.dataProvider().playerConfig(hooked.getName());

		/* create config if player's config doesnt exists */
		if (playerConfig == null) {
			playerConfig = new PlayerConfig();
			playerConfig.username = hooked.getName();
			playerConfig.token = CoID.generate().toString();

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-1");
				digest.update(hooked.getAddress().getAddress().getHostAddress().getBytes(StandardCharsets.UTF_8));
				playerConfig.address = javax.xml.bind.DatatypeConverter.printHexBinary(digest.digest());
			} catch (Exception ex) {
				playerConfig.address = "unknown";
			}

			Seriex._self.dataProvider().createPlayerConfig(hooked.getName(), playerConfig);
		}
	}

	public void addAddon(PlayerAddon addon) {
		System.out.println(":DDDDDDDDDDDDDDDDDd");
		addons.put(addon.getClass(), addon);
		addon.onActivated();
	}

	@SuppressWarnings("unchecked")
	public <X extends PlayerAddon> X addon(Class<? extends PlayerAddon> addon) {
		PlayerAddon instance = addons.getOrDefault(addon, null);
		return instance == null ? null : (X) instance;
	}

	public PlayerAddon removeAddon(Class<? extends PlayerAddon> addon) {
		PlayerAddon instance = addons.remove(addon);
		if (instance != null)
			instance.onDisabled();

		return instance;
	}


	public void onMove(PlayerMoveEvent event) {
		addons.values().forEach(x -> x.onMove(event));
	}



	public HashMap<Class<? extends PlayerAddon>, PlayerAddon> addons() {
		return addons;
	}

	@Override
	public Player getHooked() {
		return super.getHooked();
	}

	public void onJoin() { }
	public void onLeave() { }

}
