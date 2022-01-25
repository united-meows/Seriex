package pisi.unitedmeows.seriex;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.command.CommandSystem;
import pisi.unitedmeows.seriex.config.ConfigManager;
import pisi.unitedmeows.seriex.config.DatabaseDataProvider;
import pisi.unitedmeows.seriex.config.IDataProvider;
import pisi.unitedmeows.seriex.config.StelixDataProvider;
import pisi.unitedmeows.seriex.config.impl.ServerConfig;
import pisi.unitedmeows.seriex.config.impl.WorldConfigs;
import pisi.unitedmeows.seriex.listener.SeriexRawListener;
import pisi.unitedmeows.seriex.player.PlayerW;
import pisi.unitedmeows.yystal.file.YFile;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import stelix.xfile.reader.SxfReader;
import stelix.xfile.writer.SxfWriter;

import java.io.File;
import java.util.HashMap;

public class Seriex extends JavaPlugin {

	/* instance of the seriex */
	public static Seriex _self;

	private IDataProvider dataProvider;

	/* main command system */
	private CommandSystem commandSystem;

	/* player wrapper map */
	private static HashMap<Player, PlayerW> playerWrapperMap = new HashMap<>();

	/* server config */
	private ServerConfig serverConfig;

	/* world configs */
	private WorldConfigs worldConfigs;

	@Override
	public void onEnable() {
		_self = this;

		/* load server config */
		{
			if (!ConfigManager.serverConfig().exists()) {
				serverConfig = new ServerConfig();

				SxfWriter sxfWriter = new SxfWriter();
				sxfWriter.setWriteType(SxfWriter.WriteType.MULTI_LINE);
				sxfWriter.writeClassToFile(serverConfig, ConfigManager.serverConfig());
			} else {
				serverConfig = SxfReader.readObject(ServerConfig.class,
						new YFile(ConfigManager.serverConfig()).readAllText());
			}
		}

		/* worldConfigs */
		worldConfigs = new WorldConfigs();


		/* creates the command system */
		commandSystem = new CommandSystem();

		/* create the provider */
		dataProvider = setupDataProvider();


		/* listeners */
		{
			Bukkit.getServer().getPluginManager().registerEvents(new SeriexRawListener(), this);
		}

		/* commands */
		{
			Command.create("cat", "kedi", "deneme").inputs("var1", "var2").onRun(executeInfo -> {
				executeInfo.playerW().getHooked().sendRawMessage("Command has executed");
				final String var1 = executeInfo.arguments().get("var1");
				final String var2 = executeInfo.arguments().get("var2");
				executeInfo.playerW().getHooked().sendRawMessage(var1 + " " + var2);
			});
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

	@Override
	public void onDisable() {
		worldConfigs.save();
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

	public CommandSystem commandSystem() {
		return commandSystem;
	}

	public ServerConfig serverConfig() {
		return serverConfig;
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
