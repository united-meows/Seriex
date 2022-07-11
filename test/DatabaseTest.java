package test;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.database.util.DatabaseReflection.*;
import static pisi.unitedmeows.yystal.YYStal.*;

import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;

public class DatabaseTest {
	public static void main(String... args) {
		SeriexDB seriexDB = new SeriexDB(args[0], args[1], args[2], args[3], Integer.parseInt(args[4]));
		startWatcher();
		init(seriexDB);
		logger().debug(String.format("DatabaseReflection#init took %d ms", stopWatcher()));
		startWatcher();
		StructPlayer structPlayerW = seriexDB.getPlayer("tempUserkekw");
		logger().info(structPlayerW.toString());
		logger().debug(String.format("Getting player took %d ms", stopWatcher()));
	}
}
