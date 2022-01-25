package pisi.unitedmeows.seriex;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pisi.unitedmeows.seriex.config.DatabaseDataProvider;
import pisi.unitedmeows.seriex.config.IDataProvider;
import pisi.unitedmeows.seriex.config.StelixDataProvider;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;

public class Seriex extends JavaPlugin {

	/* instance of the seriex */
	public static Seriex _self;


	private IDataProvider dataProvider;

	@Override
	public void onEnable() {
		_self = this;
		dataProvider = setupDataProvider();
	}


	protected IDataProvider setupDataProvider() {
		try {
			YDatabaseClient yDatabaseClient = new YDatabaseClient("root", "12345",
					"seriex", "localhost");

			DatabaseDataProvider databaseDataProvider = new DatabaseDataProvider(yDatabaseClient);
			return databaseDataProvider;
		} catch (Exception ex) {
			return new StelixDataProvider();
		}
	}
}
