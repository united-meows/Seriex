package pisi.unitedmeows.seriex;

import java.lang.reflect.Field;
import java.util.HashMap;

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
import pisi.unitedmeows.seriex.util.BasicLogger;
import pisi.unitedmeows.yystal.file.YFile;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import stelix.xfile.reader.SxfReader;
import stelix.xfile.writer.SxfWriter;

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
	private final BasicLogger logger = new BasicLogger(getClass());

	@Override
	public void onEnable() {
		_self = this;
		/* load server config */
		{
			if (!ConfigManager.serverConfig().exists()) {
				serverConfig = new ServerConfig();
				final SxfWriter sxfWriter = new SxfWriter();
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
			logger.fatal(ChatColor.RED + "Couldn't connect to database. Using config files instead.");
		} else {
			logger.info(ChatColor.GREEN + "Database connection successful.");
		}
	}

	@Override
	public void onDisable() { worldConfigs.save(); }

	public static PlayerW playerw(final Player player) {
		return playerWrapperMap.computeIfAbsent(player, k -> new PlayerW(player));
	}

	public static PlayerW removePlayerW(final Player player) {
		return playerWrapperMap.remove(player);
	}

	public IDataProvider dataProvider() { return dataProvider; }

	public CommandSystem commandSystem() { return commandSystem; }

	public BasicLogger logger() { return logger; }

	public ServerConfig serverConfig() { return serverConfig; }

	protected static IDataProvider setupDataProvider() {
		try {
			/* connect the database */
			final YDatabaseClient yDatabaseClient = new YDatabaseClient("root", "12345", "seriex",
						"localhost");
			/* if connection is successful return the provider */
			// using reflection because i dont have latest yystal :DD:DD:D
			// TODO -> update yystal so we can use connected() from YDatabaseClient instead of reflection
			final Field field = yDatabaseClient.getClass().getDeclaredField("connected");
			field.setAccessible(true);
			final boolean connected = field.getBoolean(field);
			if (connected) return new DatabaseDataProvider(yDatabaseClient);
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		/* if db connection failed return config file provider */
		return new StelixDataProvider();
	}
}
