package pisi.unitedmeows.seriex;

import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.util.Optional.*;
import static org.bukkit.Bukkit.*;
import static pisi.unitedmeows.seriex.util.config.FileManager.*;
import static pisi.unitedmeows.seriex.util.timings.TimingsCalculator.*;
import static pisi.unitedmeows.yystal.parallel.Async.*;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.command.CommandSystem;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.listener.SeriexSpigotListener;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.AreaManager;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.managers.future.FutureManager;
import pisi.unitedmeows.seriex.managers.sign.SignManager;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.suggestion.WordList;
import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class Seriex extends JavaPlugin {
	private static Optional<Seriex> instance_;
	private CommandSystem commandSystem;
	private static FileManager fileManager;
	private static DataManager dataManager;
	private static FutureManager futureManager;
	private static SignManager signManager;
	private static SeriexDB database;
	private static DiscordBot discordBot;
	private static AuthListener authListener;
	private static AreaManager areaManager;
	private static List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private static List<Manager> managers = new GlueList<>();
	private static List<Listener> listeners = new GlueList<>();
	private static YLogger logger = new YLogger(null, "Seriex").setTime(YLogger.Time.DAY_MONTH_YEAR_FULL).setColored(true);
	private Set<Anticheat> anticheats = new HashSet<>(); // this has to be here so it can work async :D
	private static boolean loadedCorrectly; // i have an idea but it wont probably work, so this field maybe is unnecessary...
	private Thread primaryThread;
	private WorldEditPlugin worldEdit;

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
			WordList.read();
			instance_ = of(this);
			logger().info("Starting seriex...");
			primaryThread = currentThread();
			managers: {
				signManager = new SignManager();
				authListener = new AuthListener();
				setProperty("nightconfig.preserveInsertionOrder", "true");
				FormatDetector.registerExtension("seriex", TomlFormat.instance());
				GET.benchmark(temp -> {
					logger().info("Loading Managers...");
					// TODO: Add logger info for every manager (like Loading Signmanager...)
					managers.add(signManager);
					managers.add(authListener);
					managers.add(fileManager = new FileManager(getDataFolder()));
					managers.add(dataManager = new DataManager());
					// @DISABLE_FORMATTING
					DatabaseConfig config = (DatabaseConfig) fileManager.getConfig(DATABASE);
					cleanupabbleObjects.add(database = new SeriexDB(
								config.DATABASE_USERNAME.value(),
								config.DATABASE_PASSWORD.value(),
								config.DATABASE_NAME.value(),
								config.DATABASE_HOST.value(),
								config.DATABASE_PORT.value()));
					// @ENABLE_FORMATTING
					managers.add(futureManager = new FutureManager()); // this should be always last!
				}, "Managers");
				worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
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
				listeners.add(new SeriexSpigotListener());
				AreaManager areaManager = new AreaManager();
				this.areaManager = areaManager;
				listeners.add(areaManager);
				listeners.addAll(areaManager.areaList);
				listeners.forEach(listener -> getPluginManager().registerEvents(listener, this));
			}
		}
		catch (Exception e) {
			loadedCorrectly = false;
			e.printStackTrace();
		}
		/* commands */
		/* :DDD don't delete this @ghost */
		commands: {
			Command.create("cat", "kedi", "deneme").inputs("var1", "var2").onRun(executeInfo -> {
				executeInfo.playerW().getHooked().sendRawMessage("Command has executed");
				final String var1 = executeInfo.arguments().get("var1");
				final String var2 = executeInfo.arguments().get("var2");
				executeInfo.playerW().getHooked().sendRawMessage(var1 + " " + var2);
			});
		}
		/* sign manager */
		{
			SignManager.create("spawn pig").onRight((player, sign) -> {
				final Sign block = (Sign) sign.global().getIfPresent("current_sign");
				int cooldown = (int) sign.session(block).getOrDefault("cooldown", 0);
				if (cooldown == 0) {
					// do
					sign.session(block).put("cooldown", 5);
				}
			}).tick(sign -> {
				for (Map<String, Object> map : sign.session().asMap().values()) {
					int cooldown = (int) map.getOrDefault("cooldown", -1);
					if (cooldown > 0) {
						map.put("cooldown", cooldown - 1);
					}
				}
			}, 20);
		}
		/* basic areas */
		/*	AreaManager.createArea(null)
				.onEnter(p -> {
		
				})
				.onLeave(p -> {
		
				})
				.tick((area) -> {
						area.playersInArea().forEach(x-> {
						x.sendRawMessage("hello world :D");
					});
				}, 20);
		*/
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

	public Player sendMessage(Player player, String message, Object... args) {
		player.sendMessage(colorizeString(String.format("%s &7%s %s", getSuffix(), message, args)));
		return player;
	}

	private String getSuffix() {
		ServerConfig config = (ServerConfig) fileManager.getConfig(fileManager.SERVER);
		return config.MESSAGE_SUFFIX.value();
	}

	public SignManager signManager() {
		return signManager;
	}

	public SeriexDB database() {
		return database;
	}

	public Seriex discordBot(DiscordBot discordBot) {
		this.discordBot = discordBot;
		return this;
	}

	public DiscordBot discordBot() {
		return discordBot;
	}

	public CommandSystem commandSystem() {
		return commandSystem;
	}

	public WorldEditPlugin worldEdit() {
		return worldEdit;
	}

	public AreaManager areaManager() {
		return areaManager;
	}
}
