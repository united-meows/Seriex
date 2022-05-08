package pisi.unitedmeows.seriex;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.util.Optional.*;
import static org.bukkit.Bukkit.*;
import static pisi.unitedmeows.seriex.util.timings.TimingsCalculator.*;
import static pisi.unitedmeows.yystal.parallel.Async.*;

import java.io.File;
import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.StructPlayerW;
import pisi.unitedmeows.seriex.listener.SeriexSpigotListener;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.managers.future.FutureManager;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.lists.GlueList;
import pisi.unitedmeows.seriex.util.logging.SLogger;
import pisi.unitedmeows.seriex.util.yystal.FixedTaskPool;
import pisi.unitedmeows.yystal.YYStal;
import pisi.unitedmeows.yystal.sql.YSQLCommand;
import pisi.unitedmeows.yystal.utils.CoID;

public class Seriex extends JavaPlugin {
	private static Optional<Seriex> instance_;
	private static FileManager fileManager;
	private static DataManager dataManager;
	private static FutureManager futureManager;
	private static List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private SLogger logger = new SLogger(getClass());
	private Set<Anticheat> anticheats = new HashSet<>(); // this has to be here so it can work async :D
	private static boolean loadedCorrectly; // i have an idea but it wont probably work, so this field maybe is unnecessary...
	private Thread primaryThread;
	public String suffix = colorizeString("&7[&dSer&5iex&7]"); // TODO get this from server-config
	public String ghostsDiscord = colorizeString("&dfemboy ghost&8#&72173");  // TODO get this from server-config

	private SeriexDB database = new SeriexDB("seriex", "seriexdb123", "seriex",
			"79.110.234.147"); // make the ip something like (db.seriex.software)

	@Override
	public void onEnable() {
		loadedCorrectly = true;
		try {
			instance_ = of(this);
			logger().info("Starting seriex...");
			primaryThread = currentThread();
			YYStal.setCurrentPool(new FixedTaskPool(3, 15));
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
		System.out.println(CoID.generate());
		SeriexDB seriexDB = new SeriexDB("seriex", "seriexdb123", "seriex",
				"79.110.234.147");

		StructPlayerW structPlayerW = seriexDB.getPlayerW("slowcheet4h");
		out.println(structPlayerW);

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
}
