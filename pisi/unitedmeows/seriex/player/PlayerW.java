package pisi.unitedmeows.seriex.player;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.bukkit.entity.Player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.utils.CoID;

public class PlayerW extends HookClass<Player> {
	private PlayerConfig playerConfig;

	public PlayerW(final Player _player) {
		hooked = _player;
		/* tries to retrieve the player config */
		playerConfig = Seriex._self.dataProvider().playerConfig(hooked.getName());
		/* create config if player's config doesnt exists */
		if (playerConfig == null) {
			playerConfig = new PlayerConfig();
			playerConfig.username = hooked.getName();
			playerConfig.token = CoID.generate().toString();
			try {
				final MessageDigest digest = MessageDigest.getInstance("SHA-1");
				digest.update(hooked.getAddress().getAddress().getHostAddress()
							.getBytes(StandardCharsets.UTF_8));
				playerConfig.address = javax.xml.bind.DatatypeConverter.printHexBinary(digest.digest());
			} catch (final Exception ex) {
				playerConfig.address = "unknown";
			}
			Seriex._self.dataProvider().createPlayerConfig(hooked.getName(), playerConfig);
		}
	}

	@Override
	public Player getHooked() { return super.getHooked(); }

	public void onJoin() {}

	public void onLeave() {}
}
