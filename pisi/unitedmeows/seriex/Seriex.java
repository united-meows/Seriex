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
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.material.Sign;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import pisi.unitedmeows.seriex.adapters.InventoryPacketAdapter;
import pisi.unitedmeows.seriex.adapters.MOTDAdapter;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.auth.AuthListener;
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
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.MaintainersUtil;
import pisi.unitedmeows.seriex.util.Try;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.impl.server.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.I18n;
import pisi.unitedmeows.seriex.util.suggestion.WordList;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.logger.impl.YLogger;

public class Seriex extends JavaPlugin {
	private static Optional<Seriex> instance_ = Optional.empty();
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
		// TODO: for supporting /reload command we should register all online players at onEnable
		// better idea, kick all online players & disable login until plugin is initiliazed
		loadedCorrectly = true;
		try {
			WordList.read();
			instance_ = of(this);
			logger().info("Starting Seriex...");
			if (new Random().nextBoolean()) {
				logger().fatal("!!! al" + SECRET_MESSAGE);
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
				if (packetAdapters == null) {
					packetAdapters = new GlueList<>();
					logger().fatal("Packet adapters was somehow null?");
				}
				packetAdapters.clear();
				ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
				GET.benchmark(yes -> {
					packetAdapters.add(inventoryPacketAdapter = new InventoryPacketAdapter());
				}, "Inventory Packet Adapter");
				GET.benchmark(yes -> {
					packetAdapters.add(new MOTDAdapter().createAdapter(protocolManager));
				}, "MOTD Packet Adapter");
				packetAdapters.forEach(protocolManager::addPacketListener);
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
		signs: {
			SignManager.create("spawn pig").counting(10).onRight((PlayerW player, SignCommand sign) -> {
				final Sign block = (Sign) sign.global().getIfPresent("current_signMaterial");
				final org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign) sign.global().getIfPresent("current_sign");
				BlockFace blockFace = block.getFacing();
				Location locationOfSign = signBlock.getLocation();
				double offsetAmount = 1.0;
				double offsetX = blockFace.getModX() * offsetAmount;
				double offsetZ = blockFace.getModZ() * offsetAmount;
				int amountOfPigs = 5;
				BlockFace oppositeBlockFace = null;
				for (BlockFace value : BlockFace.values()) {
					boolean posX = blockFace.getModX() > 0;
					boolean posZ = blockFace.getModZ() > 0;
					boolean negX = blockFace.getModX() < 0;
					boolean negZ = blockFace.getModZ() < 0;
					boolean valuePosX = value.getModX() > 0;
					boolean valuePosZ = value.getModZ() > 0;
					if ((negZ || posZ) && valuePosX) { // NORTH & SOUTH -> EAST
						oppositeBlockFace = value;
						break;
					}
					if ((negX || posX) && valuePosZ) { // WEST & EAST -> SOUTH
						oppositeBlockFace = value;
						break;
					}
				}
				if (oppositeBlockFace == null) {
					Seriex.logger().print("Couldnt get opposite block face of sign %s", locationOfSign.toString());
					return;
				}
				Location oneBlockAheadOfSign = locationOfSign.add(offsetX, 0, offsetZ);
				List<Location> pigLocations = new ArrayList<>();
				int indexOfMiddlePig = amountOfPigs - (amountOfPigs >> 1);
				for (double i = 0; i <= amountOfPigs; i++) {
					if (i < indexOfMiddlePig) {
						pigLocations.add(oneBlockAheadOfSign.add(oppositeBlockFace.getModX() * i, -0.5, oppositeBlockFace.getModZ() * i));
					} else if (i != indexOfMiddlePig) {
						BlockFace reverse = oppositeBlockFace.getOppositeFace();
						pigLocations.add(oneBlockAheadOfSign.add(reverse.getModX() * i, -0.5, reverse.getModZ() * i));
					}
				}
				//
				//	7 6 5 4 3 2 1 0
				//	0 1 0 0 0 1 1 1
				//		0b000000000000000000000100111
				//
				PotionEffect infiniteSlowness = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10);
				Vector var1 = new Vector(0, 0, 0);
				pigLocations.forEach(location -> {
					Pig spawnedPig = (Pig) player.getHooked().getWorld().spawnEntity(location, EntityType.PIG);
					spawnedPig.setHealth(1);
					spawnedPig.setMaxHealth(1);
					spawnedPig.addPotionEffect(infiniteSlowness);
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
						spawnedPig.setVelocity(var1);
					});
				});
			});
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
			player.kickPlayer(colorizeString(String.format("%s%n&7Restarting the server...", suffix())));
		});
		cleanupabbleObjects.forEach(ICleanup::cleanup);
		instance_ = Optional.empty();
		Try.safe(temp -> System.gc(), "Couldnt invoke Java garbage cleaner!");
		super.onDisable();
	}

	public static boolean available() {
		return instance_.isPresent();
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
			player.sendMessage(colorizeString(String.format("%s &7%s", suffix(), message)));
		} else {
			player.sendMessage(colorizeString(String.format("%s &7%s %s", suffix(), message, args)));
		}
		return player;
	}

	public String suffix() {
		ServerConfig config = (ServerConfig) fileManager.getConfig(fileManager.SERVER);
		return config.MESSAGE_SUFFIX.value();
	}

	// TODO
	public String motd() {
		return "Sample MOTD";
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
