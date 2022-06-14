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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.auth.adapters.InventoryPacketAdapter;
import pisi.unitedmeows.seriex.command.Command;
import pisi.unitedmeows.seriex.command.Command.AutoCompleteInfo;
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
import pisi.unitedmeows.seriex.util.MaintainersUtil;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.I18n;
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
	private static InventoryPacketAdapter inventoryPacketAdapter;
	private static I18n i18n;
	private static List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private static List<Manager> managers = new GlueList<>();
	private static List<Listener> listeners = new GlueList<>();
	private static List<PacketAdapter> packetAdapters = new GlueList<>();
	private static YLogger logger = new YLogger(null, "Seriex").setTime(YLogger.Time.DAY_MONTH_YEAR_FULL).setColored(true);
	private Set<Anticheat> anticheats = new HashSet<>(); // this has to be here so it can work async :D
	private static boolean loadedCorrectly; // i have an idea but it wont probably work, so this field maybe is unnecessary...
	private Thread primaryThread;
	private WorldEditPlugin worldEdit;
	private static final String SECRET_MESSAGE = "࣭࣢࣯࣢ࣰ࣫࢝ࣴࣞ࢝ࣥ࣢࣯࣢";

	@Override
	public void onEnable() {
		//TODO: for supporting /reload command we should register all online players at onEnable
		loadedCorrectly = true;
		try {
			WordList.read();
			instance_ = of(this);
			logger().info("Starting Seriex...");
			if (new Random().nextBoolean()) {
				logger().fatal("!!! " + SECRET_MESSAGE);
			}
			primaryThread = currentThread();
			managers: {
				signManager = new SignManager();
				authListener = new AuthListener();
				setProperty("nightconfig.preserveInsertionOrder", "true");
				FormatDetector.registerExtension("seriex", TomlFormat.instance());
				GET.benchmark(temp -> {
					logger().info("Loading Managers...");
					GET.benchmark(yes -> {
						managers.add(signManager);
					}, "Sign Manager");
					GET.benchmark(yes -> {
						managers.add(fileManager = new FileManager(getDataFolder()));
					}, "File Manager");
					GET.benchmark(yes -> {
						managers.add(dataManager = new DataManager());
					}, "Data Manager");
					GET.benchmark(yes -> {
						// 0 iq ersin moment
						managers.add(new MaintainersUtil());
					}, "Maintainers Util");
					GET.benchmark(yes -> {
						managers.add(discordBot = new DiscordBot(fileManager));
					}, "Discord Bot");
					GET.benchmark(yes -> {
						// @DISABLE_FORMATTING
						DatabaseConfig config = (DatabaseConfig) fileManager.getConfig(DATABASE);
						cleanupabbleObjects.add(database = new SeriexDB(
									config.DATABASE_USERNAME.value(),
									config.DATABASE_PASSWORD.value(),
									config.DATABASE_NAME.value(),
									config.DATABASE_HOST.value(),
									config.DATABASE_PORT.value()));
						// @ENABLE_FORMATTING
					}, "Database");
					GET.benchmark(yes -> {
						cleanupabbleObjects.add(new I18n());
					}, "I18N");
					GET.benchmark(yes -> {
						managers.add(futureManager = new FutureManager()); // this should be always last!
					}, "Future Manager");
				}, "Managers");
				worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				GET.benchmark(temp -> DatabaseReflection.init(database), "Database Reflection");
				GET.benchmark(temp -> {
					logger().info("Enabling Managers...");
					managers.forEach((Manager manager) -> manager.start(get()));
				}, "Enabled Managers");
			}
			// sorry shit code
			packet_adapters: {
				GET.benchmark(yes -> {
					packetAdapters.add(inventoryPacketAdapter = new InventoryPacketAdapter());
				}, "Inventory Packet Adapter");
				packetAdapters.forEach(ProtocolLibrary.getProtocolManager()::addPacketListener);
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
			managers.forEach((Manager mgr) -> mgr.post(get()));
		}
		catch (Exception e) {
			loadedCorrectly = false;
			e.printStackTrace();
		}
		/* commands */
		/* :DDD don't delete this @ghost */
		commands: {
			commandSystem = new CommandSystem();
			Command.create("cat", "kedi", "deneme").inputs("var1", "var2").onRun(executeInfo -> {
				executeInfo.playerW().getHooked().sendRawMessage("Command has executed");
				final String var1 = executeInfo.arguments().get("var1");
				final String var2 = executeInfo.arguments().get("var2");
				executeInfo.playerW().getHooked().sendRawMessage(var1 + " " + var2);
			}).onAutoComplete((AutoCompleteInfo info) -> {
				info.playerW().getHooked().sendRawMessage(info.input());
				info.playerW().getHooked().sendRawMessage(info.lastToken());
				return "";
			});
		}
		/* sign manager */
		{
			SignManager.create("spawn pig").onRight((player, sign) -> {
				final Sign block = (Sign) sign.global().getIfPresent("current_sign");
				int cooldown = (int) sign.session(block).getOrDefault("cooldown", 0);
				if (cooldown == 0) {
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
		getOnlinePlayers().forEach(player -> {
			player.kickPlayer(colorizeString(String.format("%s%n&7Restarting the server...", getSuffix())));
		});
		for (int i = 0; i < cleanupabbleObjects.size(); i++) {
			cleanupabbleObjects.get(i).cleanup();
		}
		instance_ = Optional.empty();
		System.gc();
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

	public static I18n I18n() {
		return i18n;
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

	public static String colorizeString(String input) {
		return input.replace('&', '\u00A7');
	}

	public Set<Anticheat> antiCheats() {
		return new HashSet<>(anticheats);
	}

	public Player sendMessage(Player player, String message, Object... args) {
		if (args.length == 0) {
			player.sendMessage(colorizeString(String.format("%s &7%s", getSuffix(), message)));
		} else {
			player.sendMessage(colorizeString(String.format("%s &7%s %s", getSuffix(), message, args)));
		}
		return player;
	}

	public String getSuffix() {
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

	public AuthListener authentication() {
		return authListener;
	}

	public InventoryPacketAdapter inventoryPacketAdapter() {
		return inventoryPacketAdapter;
	}
}
