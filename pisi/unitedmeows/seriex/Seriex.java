package pisi.unitedmeows.seriex;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pisi.unitedmeows.seriex.config.DatabaseDataProvider;
import pisi.unitedmeows.seriex.config.IDataProvider;
import pisi.unitedmeows.seriex.config.StelixDataProvider;
import pisi.unitedmeows.seriex.listener.SeriexRawListener;
import pisi.unitedmeows.seriex.player.PlayerW;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;

import java.util.HashMap;

public class Seriex extends JavaPlugin {

	/* instance of the seriex */
	public static Seriex _self;


	private IDataProvider dataProvider;


	private static HashMap<Player, PlayerW> playerWrapperMap = new HashMap<>();


	@Override
	public void onEnable() {
		_self = this;

		/* create the provider */
		dataProvider = setupDataProvider();


		/* listeners */
		{
			Bukkit.getServer().getPluginManager().registerEvents(new SeriexRawListener(), this);
		}

		/* log the provider that plugin going to use to server cmd */
		if (dataProvider instanceof StelixDataProvider) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.YELLOW + "Couldn't connect to database. Using config files instead.");
		} else {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.GREEN + "Database connection successful.");
		}

	}


	public static PlayerW playerw(Player player) {
		return playerWrapperMap.computeIfAbsent(player, (k) ->
			new PlayerW(player)
		);
	}

	public static PlayerW removePlayerW(Player player) {
		return playerWrapperMap.remove(player);
	}

	public IDataProvider dataProvider() {
		return dataProvider;
	}

	protected static IDataProvider setupDataProvider() {
		try {

			/* connect the database */
			YDatabaseClient yDatabaseClient = new YDatabaseClient("root", "12345",
					"seriex", "localhost");

			/* if connection is successful return the provider */
			if (yDatabaseClient.connected()) {
				return new DatabaseDataProvider(yDatabaseClient);
			}
		} catch (Exception ignored) {}

		/* if db connection failed return config file provider */
		return new StelixDataProvider();
	}
}
