package test;

import static pisi.unitedmeows.seriex.Seriex.*;
import static pisi.unitedmeows.seriex.database.util.DatabaseReflection.*;
import static pisi.unitedmeows.yystal.YYStal.*;

import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;

public class DatabaseTest {
	public static void main(String... args) {
		SeriexDB seriexDB = new SeriexDB("seriex", "seriexdb123", "seriex", "79.110.234.147", 3306);
		startWatcher();
		init(seriexDB);
		logger().debug(String.format("DatabaseReflection#init took %d ms", stopWatcher()));
		startWatcher();
		StructPlayer structPlayerW = seriexDB.getPlayer("tempUserkekw");
		logger().info(structPlayerW.toString());
		logger().debug(String.format("Getting player took %d ms", stopWatcher()));
	}
}
