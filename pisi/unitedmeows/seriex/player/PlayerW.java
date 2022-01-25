package pisi.unitedmeows.seriex.player;

import org.bukkit.entity.Player;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.yystal.clazz.HookClass;
import pisi.unitedmeows.yystal.utils.CoID;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PlayerW extends HookClass<Player> {

	private PlayerConfig playerConfig;

	public PlayerW(Player _player) {
		hooked = _player;

		/* tries to retrieve the player config */
		playerConfig = Seriex._self.dataProvider().playerConfig(hooked.getName());

		/* create config if player's config doesnt exists */
		if (playerConfig == null) {
			playerConfig = new PlayerConfig();
			playerConfig.username = hooked.getName();
			playerConfig.token = CoID.generate().toString();
			System.out.println("creating the config");
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-1");
				digest.update(hooked.getAddress().getAddress().getHostAddress().getBytes(StandardCharsets.UTF_8));
				byte[] digestBytes = digest.digest();
				String digestStr = javax.xml.bind.DatatypeConverter.printHexBinary(digestBytes);
				playerConfig.address = digestStr;
			} catch (Exception ex) {
				playerConfig.address = "unknown";
			}

			Seriex._self.dataProvider().createPlayerConfig(hooked.getName(), playerConfig);
		}

	}



	public void onJoin() { }
	public void onLeave() { }

}
