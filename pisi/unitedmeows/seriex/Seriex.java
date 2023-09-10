package pisi.unitedmeows.seriex;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.derklaro.reflexion.Reflexion;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.tools.BukkitPlayerArgument;
import me.realized.duels.api.Duels;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.material.Sign;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.std.Option;
import pisi.unitedmeows.seriex.adapters.*;
import pisi.unitedmeows.seriex.anticheat.Anticheat;
import pisi.unitedmeows.seriex.api.events.EventTick;
import pisi.unitedmeows.seriex.auth.AuthManager;
import pisi.unitedmeows.seriex.commands.*;
import pisi.unitedmeows.seriex.commands.arguments.*;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.util.reflection.DatabaseReflection;
import pisi.unitedmeows.seriex.discord.DiscordBot;
import pisi.unitedmeows.seriex.listener.SeriexDuelListener;
import pisi.unitedmeows.seriex.listener.SeriexSpigotListener;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.AreaManager;
import pisi.unitedmeows.seriex.managers.area.impl.Area;
import pisi.unitedmeows.seriex.managers.area.impl.movement.SpeedTestArea;
import pisi.unitedmeows.seriex.managers.data.DataManager;
import pisi.unitedmeows.seriex.managers.data.PlayerLogger;
import pisi.unitedmeows.seriex.managers.http.WebServer;
import pisi.unitedmeows.seriex.managers.minigames.MinigameManager;
import pisi.unitedmeows.seriex.managers.multithreading.FutureManager;
import pisi.unitedmeows.seriex.managers.rank.RankManager;
import pisi.unitedmeows.seriex.managers.rank.Ranks;
import pisi.unitedmeows.seriex.managers.scoreboard.ScoreboardManager;
import pisi.unitedmeows.seriex.managers.sign.SignManager;
import pisi.unitedmeows.seriex.managers.sign.impl.SignCommand;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayer;
import pisi.unitedmeows.seriex.managers.virtualplayers.VirtualPlayerManager;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.MaintainersUtil;
import pisi.unitedmeows.seriex.util.Once;
import pisi.unitedmeows.seriex.util.PsuedoJavaPlugin;
import pisi.unitedmeows.seriex.util.collections.GlueList;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.single.impl.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.MOTDConfig;
import pisi.unitedmeows.seriex.util.config.single.impl.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.I18n;
import pisi.unitedmeows.seriex.util.language.Messages;
import pisi.unitedmeows.seriex.util.logging.LoggingOutputStream;
import pisi.unitedmeows.seriex.util.math.AxisBB;
import pisi.unitedmeows.seriex.util.math.Rotation;
import pisi.unitedmeows.seriex.util.safety.Try;
import pisi.unitedmeows.seriex.util.suggestion.WordList;
import pisi.unitedmeows.seriex.util.timings.Benchmark;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.parallel.Async;

import java.io.File;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;
import static org.bukkit.Bukkit.getOnlinePlayers;

public class Seriex implements PsuedoJavaPlugin {
	private static final Collector<CharSequence, ?, String> COLLECTOR = Collectors.joining(",", "[", "]");
	private static Seriex instance;
	private static Logger LOGGER;

	private InventoryPacketAdapter inventoryPacketAdapter;
	private LiteCommands<CommandSender> commandManager;
	private VirtualPlayerManager virtualPlayerManager;
	private ScoreboardManager scoreboardManager;
	private MinigameManager minigameManager;
	private FutureManager futureManager;
	private PlayerLogger playerLogger;
	private AuthManager authManager;
	private AreaManager areaManager;
	private DataManager dataManager;
	private RankManager rankManager;
	private FileManager fileManager;
	private SignManager signManager;
	private DiscordBot discordBot;
	private SeriexDB database;
	private Duels duels;
	private I18n i18n;

	private final List<ICleanup> cleanupabbleObjects = new GlueList<>();
	private final List<PacketAdapter> packetAdapters = new GlueList<>();
	private final List<Listener> listeners = new GlueList<>();
	private final Set<Anticheat> anticheats = new HashSet<>();
	private final List<Manager> managers = new GlueList<>();
	private final List<Once> onces = new GlueList<>();

	private boolean doneInitializing, firstStart, maintenance;
	private JavaPlugin plugin;
	private Thread primaryThread;

	protected Seriex() {}

	private void handleMinecraft(JavaPlugin plugin) {
		plugin.getServer().getLogger().setLevel(Level.ALL);
		File firstTime = new File(plugin.getDataFolder(), "first" + FileManager.EXTENSION);
		boolean firstTimeFileDoesNotExists = !firstTime.exists();
		if (firstTimeFileDoesNotExists) {
			firstStart = true;
		}
		logger().info("Starting Seriex...");
		Benchmark.profile(() -> {
			this.duels = (Duels) Bukkit.getServer().getPluginManager().getPlugin("Duels");
		}, "Duels API");
		Benchmark.profile(() -> {
			logger().info("Loading Managers...");
			Benchmark.profile(() -> {
				managers.add(authManager = new AuthManager());
			}, "Auth listener");
			Benchmark.profile(() -> {
				managers.add(signManager = new SignManager());
			}, "Sign Manager");
			Benchmark.profile(() -> {
				managers.add(fileManager = new FileManager(plugin.getDataFolder()));
			}, "File Manager");
			Benchmark.profile(() -> {
				managers.add(minigameManager = new MinigameManager());
			}, "Minigame Manager");
			Benchmark.profile(() -> {
				managers.add(dataManager = new DataManager());
			}, "Data Manager");
			Benchmark.profile(() -> {
				managers.add(scoreboardManager = new ScoreboardManager());
			}, "Sign Manager");
			Benchmark.profile(() -> {
				managers.add(new MaintainersUtil());
			}, "Maintainers Util");
			Benchmark.profile(() -> {
				managers.add(discordBot = new DiscordBot(fileManager));
			}, "Discord Bot");
			Benchmark.profile(() -> {
				database = new SeriexDB(fileManager.config(DatabaseConfig.class));
			}, "Database");
			Benchmark.profile(() -> {
				managers.add(new WebServer());
			}, "Web server");
			Benchmark.profile(() -> {
				managers.add(virtualPlayerManager = new VirtualPlayerManager());
			}, "Virtual Player Manager");
			Benchmark.profile(() -> {
				managers.add(areaManager = new AreaManager());
			}, "Area Manager");
			Benchmark.profile(() -> {
				managers.add(rankManager = new RankManager());
			}, "Rank Manager");
			Benchmark.profile(() -> {
				managers.add(playerLogger = new PlayerLogger());
			}, "Player Logger");
			Benchmark.profile(() -> cleanupabbleObjects.add(i18n = new I18n()), "I18N");
			Benchmark.profile(Anticheat::initializeClass, "Anticheat system");
			Benchmark.profile(() -> {
				this.commandManager = LiteBukkitFactory
							.builder(plugin.getServer(), plugin.getName())
							.command(
										AreaCommand.class,
										GiveCommand.class,
										LookupCommand.class,
										PotionCommand.class,
										RankCommand.class,
										VirtualPlayerCommand.class,
										GeneralCommands.class,
										OperatorCommands.class,
										TeleportCommand.class
							)
							.argumentMultilevel(Location.class, new LocationArgument())
							.argumentMultilevel(AxisBB.class, new AxisBBArgument())
							.argumentMultilevel(Rotation.class, new RotationArgument())
							.argument(Player.class, new BukkitPlayerArgument<>(plugin.getServer(), "Player not found."))
							.argument(World.class, new WorldArgument(plugin.getServer()))
							.argument(GameMode.class, new GamemodeArgument())
							.argument(PlayerW.class, new PlayerWArgument())
							.argument(StructPlayer.class, new StructPlayerArgument())
							.argument(Material.class, new MaterialArgument())
							.argument(PotionType.class, new PotionTypeArgument())
							.argument(Ranks.class, new RankArgument())
							.argument(Area.class, new AreaArgument())
							.argument(Anticheat.class, new AnticheatArgument())
							.argument(Enchantment.class, new EnchantmentArgument())
							.argument(VirtualPlayer.class, new VirtualPlayerArgument())
							.contextualBind(PlayerW.class, (sender, invocation) -> Option.of(sender).is(Player.class).map(player -> dataManager.user(player)).toResult("Player only."))
							.invalidUsageHandler((sender, invocation, schematic) -> {
								if (!(sender instanceof Player senderPlayer))
									return;

								PlayerW user = dataManager.user(senderPlayer);
								List<String> schematics = schematic.getSchematics();

								if (schematics.size() == 1) {
									Seriex.get().msg(user, Messages.COMMAND_WRONG_USAGE, schematics.get(0));
									return;
								}

								Seriex.get().msg(user, Messages.COMMAND_WRONG_USAGES);
								for (String sch : schematics) {
									Seriex.get().msg_no_translation(user, " - %s", sch);
								}
							})
							.permissionHandler((sender, invocation, permissions) -> {
								if (!(sender instanceof Player senderPlayer))
									return;

								PlayerW user = dataManager.user(senderPlayer);
								Seriex.get().msg(user, Messages.COMMAND_NO_PERMISSION, "&7[" + String.join(", ", permissions.getPermissions()) + "]");
							})
							.register();

			}, "Command System");
			// this should be always last
			Benchmark.profile(() -> managers.add(futureManager = new FutureManager()), "Future Manager");

			cleanupabbleObjects.addAll(managers);
		}, "Managers");
		Benchmark.profile(() -> {
			if (database == null) {
				String text = firstStart ? "Database isn't configured correctly!" : "Database file is corrupt?!?!";
				logger().error(text);
				return;
			}
			if (!database.connected()) {
				logger().error("Database cannot connect!");
				return;
			}
			DatabaseReflection.init(database);
		}, "Database Reflection");
		Benchmark.profile(() -> {
			logger().info("Enabling Managers...");
			managers.forEach((Manager manager) -> manager.start(get()));
		}, "Initialization of managers");
		packetAdapters.clear();
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		Benchmark.profile(() -> packetAdapters.add(inventoryPacketAdapter = new InventoryPacketAdapter()), "Inventory Packet Adapter");
		Benchmark.profile(() -> packetAdapters.add(MOTDAdapter.createAdapter(protocolManager)), "MOTD Packet Adapter");
		Benchmark.profile(() -> packetAdapters.add(Log4JAdapter.createAdapter()), "Log4J Packet Adapter");
		Benchmark.profile(() -> packetAdapters.add(VirtualPlayerManager.createAdapter()), "Area Packet Adapter");
		Benchmark.profile(() -> packetAdapters.add(DurabilityPatchAdapter.createAdapter()), "Durability Patch Adapter");
		Benchmark.profile(() -> packetAdapters.add(new LimitAdapter()), "Packet Limit Adapter");
		packetAdapters.forEach(protocolManager::addPacketListener);
		logger().info("Starting threads...");
		Async.async_loop(futureManager::updateFutures, 1);
		Benchmark.profile(() -> {
			listeners.add(new SeriexSpigotListener());
			listeners.add(new SeriexDuelListener());
			listeners.add(authManager);
			listeners.add(areaManager);
			listeners.forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
		}, "Registering listeners");
		onces.add(discordBot);
		managers.forEach((Manager mgr) -> mgr.post(get()));
		if (firstTimeFileDoesNotExists) {
			boolean created = false;
			Exception ex = null;
			try {
				created = firstTime.createNewFile();
			}
			catch (Exception e) {
				ex = e;
			}
			if (created) {
				onces.forEach(Once::once);
			} else {
				if (ex != null)
					ex.printStackTrace();
				logger().error("Couldnt create first file... disabling server!");
				System.exit(0x87D);
			}
		}
	}

	private void handleJansi() {
		try {
			// bypass shading
			var klass = Class.forName("org.fusesource.j4nsi.AnsiConsole".replace("4", "a"));
			var method = Reflexion.on(klass).findMethod("systemUninstall").orElseThrow();
			var installed = (int) Reflexion.on(klass).findField("installed").orElseThrow().getValue().getOrThrow();
			var previousOut = (PrintStream) Reflexion.on(klass).findField("system_out").orElseThrow().getValue().getOrThrow();
			var previousErr = (PrintStream) Reflexion.on(klass).findField("system_err").orElseThrow().getValue().getOrThrow();

			var previousOut0 = System.out;
			var previousErr0 = System.err;

			System.out.println("uninstalling");
			for (int i = 0; i < installed + 1; i++) {
				method.invoke().ifSuccess(yes -> System.out.println("uninstalled succesfully"));
			}

			System.out.println("test 1");
			System.setOut(previousOut);
			System.setErr(previousErr);
			System.out.println("test 2");
			System.setOut(previousOut0);
			System.setErr(previousErr0);
			System.out.println("test 3");

			AnsiConsole.systemInstall();
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void handleMain() {
		if (instance != null)
			throw SeriexException.create("Instance already exists");

		instance = this;
		primaryThread = currentThread();
		LOGGER = LoggerFactory.getLogger(Seriex.class);

		System.setOut(new PrintStream(new LoggingOutputStream(LOGGER, false), true));
		System.setErr(new PrintStream(new LoggingOutputStream(LOGGER, true), true));

		System.setProperty("nightconfig.preserveInsertionOrder", "true");
		FormatDetector.registerExtension("seriex", TomlFormat.instance());
		WordList.read();
	}

	private int serverTick;

	@Override
	public void onEnable(JavaPlugin plugin) {
		try {
			this.plugin = plugin;
			Benchmark.profile(this::handleMain, "Main initialization");
			Benchmark.profile(() -> handleMinecraft(plugin), "Plugin initialization");
			Benchmark.profile(this::initializeSigns, "Sign initialization");
			Seriex.get().runLater(() -> {
				Seriex.get().discordBot().addMessageToQueue("Server is online!");
			}, 50);

			new BukkitRunnable() {
				@Override
				public void run() {
					EventTick event = new EventTick(serverTick++);
					dataManager.users().forEach(user -> user.fireEvent(event));
				}
			}.runTaskTimer(plugin, 0, 1L);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger().error("Couldnt load plugin correctly!");
			System.exit(-1);
			return;
		}
		doneInitializing = true;
		if (firstStart) {
			logger().info("Please wait for 5 seconds...");
			Async.async(() -> {
				logger().info("Created all config files & stuff, configure your plugin and re-launch the server!");
				System.exit(0);
			}, 5000L);
		}
	}

	@Override
	public void onDisable(JavaPlugin plugin) {
		commandManager.getPlatform().unregisterAll();
		getOnlinePlayers().forEach((Player player) -> kick(player, Messages.SERVER_RESTART));
		cleanupabbleObjects.forEach(ICleanup::cleanup);
		Try.safe(System::gc);
	}

	public static Seriex get() {
		if (instance == null)
			throw SeriexException.create("Instance is not present.");

		return instance;
	}

	public void run(Runnable runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTask(plugin);
	}

	public void runAsync(Runnable runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskAsynchronously(plugin);
	}

	public void runLater(Runnable runnable, long ticks) {
		if (ticks == 0) {
			run(runnable);
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLater(plugin, ticks);
	}

	public Thread primaryThread() {
		return primaryThread;
	}

	public Logger logger() {
		return LOGGER;
	}

	public I18n I18n() {
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

	public VirtualPlayerManager virtualPlayerManager() {
		return virtualPlayerManager;
	}

	public RankManager rankManager() {
		return rankManager;
	}

	public static String colorizeString(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	/**
	 * This set is modifiable, but it is not recommended to modify the set
	 */
	public Set<Anticheat> antiCheats() {
		return anticheats;
	}

	/*
	 * TODOL: should we move these msg,kick,msg_notranslation... etc methods to somewhere else?
	 */
	public Player msg(PlayerW player, Messages message, Object... args) {
		return msg(player.hook(), message, args);
	}

	public Player msg(Player player, Messages message, Object... args) {
		String convertedMsg = I18n().getMessage(message, dataManager().user(player));
		return msg_no_translation(player, convertedMsg, args);
	}

	public Player msg_state(PlayerW user, Messages message, boolean state) {
		String convertedMsg = I18n().getMessage(message, user);
		return msg_no_translation(user, convertedMsg, state
					? "&a" + I18n().getMessage(Messages.COMMAND_ENABLED, user)
					: "&c" + I18n().getMessage(Messages.COMMAND_DISABLED, user));
	}

	public Player msg_state(Player player, Messages message, boolean state) {
		PlayerW user = dataManager().user(player);
		return msg_state(user, message, state);
	}

	public void kick(Player player, Messages message, Object... args) {
		String convertedMsg = I18n().getMessage(message, dataManager().user(player));
		kick_no_translation(player, convertedMsg, args);
	}

	/**
	 * Should only be used if we can`t retrieve the players language, i.e. before the player logged in.
	 */
	@SuppressWarnings("all")
	public void kick_no_translation(Player player, String message, Object... args) {
		String format = String.format("%s\n&7%s", suffix(), message);
		player.kickPlayer(handleFormatting(format, args));
	}

	/**
	 * Should only be used for debugging or messages that cant be translated. (i.e. flag messages, additions extra to the translated message)
	 */
	public Player msg_no_translation(PlayerW player, String message, Object... args) {
		return msg_no_translation(player.hook(), message, args);
	}

	/**
	 * Should only be used for debugging or messages that cant be translated. (i.e. flag messages, additions extra to the translated message)
	 */
	public Player msg_no_translation(Player player, String message, Object... args) {
		String format = String.format("%s &7%s", suffix(), message);
		player.sendMessage(handleFormatting(format, args));
		return player;
	}

	public Player msg_click_on_copy(Player player, String toCopy, Object... args) {
		String format = String.format("%s &7%s", suffix(), I18n().getMessage(Messages.SERVER_CLICK_TO_COPY, dataManager.user(player)));
		TextComponent component = new TextComponent(handleFormatting(format, args));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy));
		player.spigot().sendMessage(component);
		return player;
	}

	public Player msg_click_on_copy_no_translation(Player player, String toCopy, String message, Object... args) {
		String format = String.format("%s &7%s", suffix(), message);
		TextComponent component = new TextComponent(handleFormatting(format, args));
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, toCopy));
		player.spigot().sendMessage(component);
		return player;
	}

	private String handleFormatting(String message, Object... args) {
		if (args.length == 0) {
			return colorizeString(message);
		} else {
			return colorizeString(String.format(message, args));
		}
	}

	public String suffix() {
		ServerConfig config = fileManager.config(ServerConfig.class);
		return config.MESSAGE_SUFFIX.value();
	}

	public String motd() {
		return ((MOTDConfig) fileManager.config(MOTDConfig.class)).randomMOTD();
	}

	public Duels duels() {
		return duels;
	}

	public ScoreboardManager scoreboardManager() {
		return scoreboardManager;
	}

	public SignManager signManager() {
		return signManager;
	}

	public SeriexDB database() {
		return database;
	}

	public DiscordBot discordBot() {
		return discordBot;
	}

	public AreaManager areaManager() {
		return areaManager;
	}

	public PlayerLogger playerLogger() {
		return playerLogger;
	}

	public AuthManager authentication() {
		return authManager;
	}

	public InventoryPacketAdapter inventoryPacketAdapter() {
		return inventoryPacketAdapter;
	}

	public MinigameManager minigameManager() {
		return minigameManager;
	}

	public LiteCommands<CommandSender> commandManager() {
		return commandManager;
	}

	public JavaPlugin plugin() {
		return plugin;
	}

	public boolean doneInitializing() {
		return doneInitializing;
	}

	public boolean maintenance() {
		return maintenance;
	}

	public void maintenance(boolean state) {
		this.maintenance = state;
	}

	public int serverTick() {
		return serverTick;
	}

	private void initializeSigns() {
		SignManager.create("speedtest").counting(60).onRight((PlayerW player, SignCommand sign) -> {
			Player hooked = player.hook();
			Optional<SpeedTestArea> optional = areaManager.areaList.stream()
						.filter(SpeedTestArea.class::isInstance)
						.filter(Area::isReallyConfigured)
						.map(SpeedTestArea.class::cast)
						.filter(area -> !area.inUse).min((area1, area2) -> {
							Location location = player.hook().getLocation();
							return Double.compare(area1.warpLocation.distance(location), area2.warpLocation.distance(location));
						});
			if (optional.isPresent()) {
				msg(hooked, Messages.AREA_SPEEDTEST_QUEUED);
				SpeedTestArea speed_test = optional.get();
				Seriex.get().runLater(() -> speed_test.handleEnter(hooked), 10);
			} else {
				msg(player.hook(), Messages.AREA_SPEEDTEST_ERROR);
			}
		});
		SignManager.create("spawn pig").counting(20).onRight((PlayerW player, SignCommand sign) -> {
			final Sign block = (Sign) sign.global().getIfPresent("current_signMaterial");
			final org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign) sign.global().getIfPresent("current_sign");
			BlockFace facing = block.getFacing();
			Location locationOfSign = signBlock.getLocation();
			double offsetAmount = 1.0;
			double offsetX = facing.getModX() * offsetAmount;
			double offsetZ = facing.getModZ() * offsetAmount;
			int amountOfPigs = 5;
			Location oneBlockAheadOfSign = locationOfSign.add(offsetX, 0, offsetZ);
			List<Location> pigLocations = new ArrayList<>();
			BlockFace spawnFacing = switch (facing) {
				case NORTH, SOUTH ->  BlockFace.EAST;
				case WEST, EAST -> BlockFace.SOUTH;
				default -> null;
			};

			if(spawnFacing == null) {
				logger().error("Couldnt find correct facing for sign with the facing '{}'", facing.name());
				return;
			}

			for (int i = 0; i < amountOfPigs; i++) {
				int fixedIndex = -2 + i;
				pigLocations.add(oneBlockAheadOfSign.clone().add(
										(double) spawnFacing.getModX() * fixedIndex, 0,
										(double) spawnFacing.getModZ() * fixedIndex)
							.add(0.5D, -1, 0.5D));
			}
			PotionEffect infiniteSlowness = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10);
			Vector zero_vector = new Vector(0, 0, 0);
			pigLocations.forEach(location -> {
				Pig spawnedPig = (Pig) player.hook().getWorld().spawnEntity(location, EntityType.PIG);
				spawnedPig.setHealth(2);
				spawnedPig.setMaxHealth(2);
				spawnedPig.addPotionEffect(infiniteSlowness);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
					spawnedPig.setVelocity(zero_vector);
				}, 1L);
			});
		});
	}

	public Collector<CharSequence, ?, String> collector() {
		return COLLECTOR;
	}

	/**
	 * @return GMT+3 time for Seriex`s current hosted machine
	 */
	public long fixedMS() {
		return ZonedDateTime
					.ofInstant(Instant.now(), ZoneId.of("Turkey")) // get GMT+3 time
					.toInstant()
					.toEpochMilli(); // convert to MS
	}
}
