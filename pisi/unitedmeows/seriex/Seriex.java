package pisi.unitedmeows.seriex;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.util.Optional.*;
import static org.bukkit.Bukkit.*;
import static pisi.unitedmeows.seriex.util.timings.TimingsCalculator.*;
import static pisi.unitedmeows.yystal.parallel.Async.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.command.CommandSystem;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.StructPlayer;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.listener.SeriexSpigotListener;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.managers.future.FutureManager;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.lists.GlueList;
import pisi.unitedmeows.seriex.util.logging.SLogger;
import pisi.unitedmeows.yystal.YYStal;

public class Seriex extends JavaPlugin {
	private static Optional<Seriex> instance_;
	private CommandSystem commandSystem;
	private static FileManager fileManager;
	private static DataManager dataManager;
	private static FutureManager futureManager;
	public static SeriexDB database = new SeriexDB("seriex", "seriexdb123", "seriex", "79.110.234.147"); //TODO: get this values from a config file
	private static List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private SLogger logger = new SLogger(getClass());
	private Set<Anticheat> anticheats = new HashSet<>(); // this has to be here so it can work async :D
	private static boolean loadedCorrectly; // i have an idea but it wont probably work, so this field maybe is unnecessary...
	private Thread primaryThread;
	public String suffix = colorizeString("&7[&dSer&5iex&7]"); // TODO get this from server-config
	public String ghostsDiscord = colorizeString("&dfemboy ghost&8#&72173");  // TODO get this from server-config

	@Override
	public void onEnable() {
		//TODO: for supporting /reload command we should register all online players at onEnable
		loadedCorrectly = true;
		try {
			instance_ = of(this);
			logger().info("Starting seriex...");
			primaryThread = currentThread();
			GET.benchmark(temp -> DatabaseReflection.init(), "Database Reflection");
			managers: {
				setProperty("nightconfig.preserveInsertionOrder", "true");
				FormatDetector.registerExtension("seriex", TomlFormat.instance());
				GET.benchmark(temp -> {
					logger().info("Loading Managers...");
					cleanupabbleObjects.add(fileManager = new FileManager(getDataFolder()));
					cleanupabbleObjects.add(dataManager = new DataManager());
					cleanupabbleObjects.add(futureManager = new FutureManager()); // this should be always last!
				}, "Managers");
			}
			async_stuff: {
				// 🤞 🤞 🤞 🤞 🤞
				logger().info("Starting threads...");
				async_loop(futureManager::updateFutures, 1);
			}
			listeners: {
				logger().info("Registering listeners...");
				getPluginManager().registerEvents(new SeriexSpigotListener(), this);
			}
		}
		catch (Exception e) {
			loadedCorrectly = false;
			e.printStackTrace();
		}
		/* maybe make similar thing for small areas like */
		/* Area.create(x, y, z, x1, y2, z3).(p ->
		{
			p.heal(5)
		}) ;
		 regenerates heal when player is inside (runs on every tick)
		 and events like onEnter() onLeave()
		 */
		/* commands */
		/* :DDD don't delete this @ghost */
		{
			Command.create("cat", "kedi", "deneme").inputs("var1", "var2").onRun(executeInfo -> {
				executeInfo.playerW().getHooked().sendRawMessage("Command has executed");
				final String var1 = executeInfo.arguments().get("var1");
				final String var2 = executeInfo.arguments().get("var2");
				executeInfo.playerW().getHooked().sendRawMessage(var1 + " " + var2);
			});
		}
		super.onEnable();
	}

	@Override
	public void onDisable() {
		// maybe this is slower than using a for loop for the getOnlinePlayers collection
		List<Player> tempPlayers = new GlueList<>(getOnlinePlayers());
		for (int i = 0; i < tempPlayers.size(); i++) {
			Player player = tempPlayers.get(i);
			player.kickPlayer(suffix + "\n" + "Restarting the server...");
		}
		for (int i = 0; i < cleanupabbleObjects.size(); i++) {
			ICleanup cleanup = cleanupabbleObjects.get(i);
			cleanup.cleanup();
		}
		dataManager = null;
		anticheats = null;
		fileManager = null;
		futureManager = null;
		instance_ = Optional.empty(); // this should be last.
		super.onDisable();
	}

	public static Seriex get() {
		return instance_.orElseThrow(() -> new SeriexException("Seriex has not been loaded correctly!"));
	}

	public static void main(String... args) throws Exception {
		DatabaseReflection.init();
		YYStal.startWatcher();
		StructPlayer structPlayerW = database.getPlayerW("slowcheet4h");
		out.println(structPlayerW);
		System.out.println("#1 " + YYStal.stopWatcher());
	}

	public Thread primaryThread() {
		return primaryThread;
	}

	public SLogger logger() {
		return logger;
	}

	public FutureManager futureManager() {
		return futureManager;
	}

	public DataManager dataManager() {
		return dataManager;
	}

	public FileManager fileManager() {
		return fileManager;
	}

	public String colorizeString(String input) {
		return input.replace('&', '\u00A7');
	}

	public Set<Anticheat> antiCheats() {
		return new HashSet<>(anticheats);
	}

	public SeriexDB database() {
		return database;
	}

	public CommandSystem commandSystem() {
		return commandSystem;
	}
}
