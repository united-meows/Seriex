package pisi.unitedmeows.seriex;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.util.Optional.*;
import static org.bukkit.Bukkit.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;
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
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.listener.SeriexSpigotListener;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.managers.future.FutureManager;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.YYStal;
import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class Seriex extends JavaPlugin {
	private static Optional<Seriex> instance_;
	private CommandSystem commandSystem;
	private static FileManager fileManager;
	private static DataManager dataManager;
	private static FutureManager futureManager;
	private static SeriexDB database;
	private static List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private static List<Manager> managers = new GlueList<>();
	private static YLogger logger = new YLogger(null, "Seriex").setTime(YLogger.Time.DAY_MONTH_YEAR_FULL).setColored(true);
	private Set<Anticheat> anticheats = new HashSet<>(); // this has to be here so it can work async :D
	private static boolean loadedCorrectly; // i have an idea but it wont probably work, so this field maybe is unnecessary...
	private Thread primaryThread;

	@Override
	public void onEnable() {
		//TODO: for supporting /reload command we should register all online players at onEnable
		/*
		 * if(true) {
		 * 	test.unsafeStart();
		 * 	return;
		 * }
		 */
		loadedCorrectly = true;
		try {
			instance_ = of(this);
			logger().info("Starting seriex...");
			primaryThread = currentThread();
			managers: {
				setProperty("nightconfig.preserveInsertionOrder", "true");
				FormatDetector.registerExtension("seriex", TomlFormat.instance());
				GET.benchmark(temp -> {
					logger().info("Loading Managers...");
					managers.add(fileManager = new FileManager(getDataFolder()));
					managers.add(dataManager = new DataManager());
					// @DISABLE_FORMATTING
					Config config = fileManager.getConfig(SETTINGS);
					cleanupabbleObjects.add(database = new SeriexDB(
								config.getValue("database.username", config.config),
								config.getValue("database.password", config.config),
								config.getValue("database.name", config.config),
								config.getValue("database.host", config.config),
								config.getValue("database.port", config.config)));
					// @ENABLE_FORMATTING
					managers.add(futureManager = new FutureManager()); // this should be always last!
				}, "Managers");
				GET.benchmark(temp -> DatabaseReflection.init(database), "Database Reflection");
				GET.benchmark(temp -> {
					logger().info("Enabling Managers...");
					managers.forEach((Manager manager) -> manager.start(get()));
				}, "Enabled Managers");
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
			player.kickPlayer(getSuffix() + "\n" + "Restarting the server...");
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
		return instance_.orElseThrow(() -> new SeriexException("Seriex isnt loaded properly!"));
	}
	//	public static ghost.virtualjava.VirtualThread<Seriex> test = new ghost.virtualjava.VirtualThread<>(get(), new VirtualTask<>((ghost.virtualjava.VirtualThread thread, Seriex seriex, ghost.virtualjava.VirtualTask... tasks) -> {
	//		logger().debug("Enabled Seriex using VirtualThread!");
	//		ghost.virtualjava.VirtualTask task = tasks[0]; // (TODOH) make it multi taskable? or something
	//		if (task.isCompleted()) {
	//			onDisable(); // ok spigot cries here find out why (TODOL)
	//			logger().fatal("Disabling Seriex..."); // less go??
	//			thread.parkOtherThreads(futureManager::isntDone); // this uses unsafe, spigot might cry
	//			thread.dead(true); // kys thread LOL
	//		} else {
	//			thread.updateTask(task, seriex); // update task
	//			thread.thankYouOracle((~Integer.MIN_VALUE & thread.aliveTicks) != 0); // allows us to call removeUsingUnsafe
	//			if (task.updated()) {
	//				task.run(thread.size() - 1); // look for the last task to check if we are still in queue
	//				task.updated(false); // stop updating to call other tasks
	//			}
	//		}
	//	}).finish(thread -> thread::removeUsingUnsafe)).death(thread -> thread::interrupt).every(thread -> logger().fatal(thread.getUnsafe().ensureClassInitialized(seriex.getClass())))
	//				.compat(false) /* fuck java >8 LOL */
	//				.safe(true);

	public static void main(String... args) throws NoSuchFieldException,SecurityException,IllegalArgumentException,IllegalAccessException {
		//		sun.misc.Unsafe unsafe = null;
		//		Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		//		theUnsafe.setAccessible(true);
		//		unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
		//		long l = unsafe.allocateMemory(512L);
		//		try {
		//			unsafe.putLong(l, Primitives.hash(1408390939117193108L, 515224124L));
		//			byte ayoThePizzasHere = unsafe.getByte(l);
		//			logger().fatal(String.format("%d", Primitives.unsignedByte(ayoThePizzasHere)));
		//		}
		//		finally {
		//			unsafe.freeMemory(l);
		//		}
		//		CommentedConfig config = CommentedConfig.inMemoryConcurrent();
		//		setProperty("nightconfig.preserveInsertionOrder", "true");
		//		List<String> adresses = new ArrayList<>();
		//		for (int i = 10; i > 0; i--) {
		//			adresses.add("seriex.example_permission" + i);
		//		}
		//		config.set("hey", true);
		//		config.set("ADMIN.internal", "admin");
		//		config.set("ADMIN.shortcut", "seriex.admin");
		//		config.set("ADMIN.coolName", "&7[&cAdmin&7]");
		//		config.set("ADMIN.permissions", adresses);
		//		config.set("HELPER.internal", "helper");
		//		config.set("HELPER.shortcut", "seriex.helper");
		//		config.set("HELPER.coolName", "&7[&dHelper&7]");
		//		config.set("HELPER.permissions", adresses);
		//		System.out.println("Config: " + config);
		//		Object object = config.get("ADMIN");
		//		System.out.println(object);
		//		File configFile = new File("commentedConfig.toml");
		//		TomlWriter writer = new TomlWriter();
		//		writer.write(config, configFile, WritingMode.REPLACE);
		SeriexDB seriexDB = new SeriexDB("seriex", "seriexdb123", "seriex", "79.110.234.147", 3306);
		DatabaseReflection.init(seriexDB);
		YYStal.startWatcher();
		StructPlayer structPlayerW = seriexDB.getPlayer("tempUserkekw");
		out.println(structPlayerW);
		logger().debug("#1 " + YYStal.stopWatcher());
	}

	public Thread primaryThread() {
		return primaryThread;
	}

	public static YLogger logger() {
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

	public String getSuffix() {
		Config config = fileManager.getConfig(fileManager.SETTINGS);
		return config.getValue("server.msg_suffix", config.config);
	}

	public SeriexDB database() {
		return database;
	}

	public CommandSystem commandSystem() {
		return commandSystem;
	}
}
