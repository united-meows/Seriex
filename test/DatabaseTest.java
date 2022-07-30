package test;

import static pisi.unitedmeows.seriex.Seriex.logger;
import static pisi.unitedmeows.seriex.database.util.DatabaseReflection.init;
import static pisi.unitedmeows.yystal.YYStal.startWatcher;
import static pisi.unitedmeows.yystal.YYStal.stopWatcher;

import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.util.math.Hashing;

public class DatabaseTest {
	public static void main(String... args) {
		SeriexDB seriexDB = new SeriexDB(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]));
		startWatcher();
		init(seriexDB);
		logger().debug(String.format("DatabaseReflection#init took %d ms", stopWatcher()));
		logger().debug(seriexDB.getPlayer("ghost2173").toString());
		if (args.length != 6) return;
		startWatcher();
		StructPlayer structPlayer = new StructPlayer();
		String username = "tempUser";
		structPlayer.username = username;
		structPlayer.salt = Hashing.randomString(8);
		structPlayer.firstLogin = true;
		structPlayer.password = Hashing.hashedString(structPlayer.salt + "lololo");
		structPlayer.create(seriexDB);
		logger().debug(String.format("Creating user took %d ms", stopWatcher()));
		StructPlayer structPlayerW = seriexDB.getPlayer(username);
		if (structPlayerW == null) {
			logger().fatal("couldnt get player!");
			System.exit(-1);
			return;
		}
		startWatcher();
		logger().info(structPlayerW.toString());
		logger().debug(String.format("Getting player took %d ms", stopWatcher()));
		startWatcher();
		StructPlayer modify = seriexDB.getPlayer(username);
		modify.banned = true;
		modify.update(seriexDB);
		logger().debug(String.format("Modifying player took %d ms", stopWatcher()));
		startWatcher();
		logger().info(seriexDB.getPlayer(username).toString());
		logger().debug(String.format("Getting modified player took %d ms", stopWatcher()));
		System.exit(0);
	}
}
