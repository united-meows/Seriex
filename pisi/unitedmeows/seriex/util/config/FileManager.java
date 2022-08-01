package pisi.unitedmeows.seriex.util.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static pisi.unitedmeows.seriex.Seriex.get;
import static pisi.unitedmeows.seriex.Seriex.logger;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.server.AreaConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.AuthConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.BanActionsConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.DatabaseConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.DiscordConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.MaintainersConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.MinigameConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.RanksConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.WorldConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;
import pisi.unitedmeows.yystal.utils.Pair;

public class FileManager extends Manager {
	public static final String AUTH = "auth";
	public static final String EMOTES = "global_emotes";
	public static final String SERVER = "server";
	public static final String EMOTE = "emote";
	public static final String BAN_ACTIONS = "banActions";
	public static final String RANKS = "ranks";
	public static final String MAINTAINERS = "maintainers";
	public static final String WORLD = "world";
	public static final String AREAS = "areas";
	public static final String TRANSLATIONS = "translations";
	public static final String DATABASE = "database";
	public static final String DISCORD = "discord";
	public static final String MINIGAME = "minigame";
	public static final String EXTENSION = ".seriex";
	public static final String PRIVATE = "#PRIVATE#";
	private final Map<String, Pair<File, Config>> fileVariablesMap = new HashMap<>();
	public static File directory , saved;
	public static boolean set;
	private boolean allDone = true;
	private Map<String, Boolean> verifier = new HashMap<>();

	public FileManager(File pluginDirectory) {
		this.directory = pluginDirectory;
		if (!set) {
			this.saved = pluginDirectory;
			File translationsFile = new File(directory, TRANSLATIONS);
			File worldDirectory = new File(directory, WORLD);
			File minigameDirectory = new File(directory, MINIGAME);
			if (Seriex.available()) {
				createFile(verifier, WORLD, new WorldConfig(worldDirectory, EXTENSION, get().getServer().getWorlds().stream().toArray(World[]::new)));
				createFile(verifier, MINIGAME, new MinigameConfig(minigameDirectory, EXTENSION, get().minigameManager().minigames()));
			}
			createFile(verifier, TRANSLATIONS, new TranslationsConfig(translationsFile, EXTENSION, Language.values()));
			createFile(verifier, DISCORD, new DiscordConfig());
			createFile(verifier, DATABASE, new DatabaseConfig());
			createFile(verifier, AUTH, new AuthConfig());
			createFile(verifier, BAN_ACTIONS, new BanActionsConfig());
			createFile(verifier, RANKS, new RanksConfig());
			createFile(verifier, MAINTAINERS, new MaintainersConfig());
			createFile(verifier, SERVER, new ServerConfig());
			// todo use @Cfg to get configs from reflection ✌
			set = true;
		}
	}

	@Override
	public void post(Seriex seriex) {
		File areasDirectory = new File(directory, AREAS);
		createFile(verifier, AREAS, new AreaConfig(areasDirectory, EXTENSION, get().areaManager().areaList.toArray(new Area[0])));
		verifier.forEach((string, bool) -> {
			allDone &= bool;
		});
		if (allDone) {
			seriex.logger().fatal("Restart the server, config files have been created!");
			System.exit(0);
		} else {
			verifier.forEach((string, bool) -> {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(string);
				stringBuilder.append(" : ");
				stringBuilder.append(bool);
				seriex.logger().debug(stringBuilder.toString());
			});
		}
		super.post(seriex);
	}

	public void writeString(final File file, final String bytes) {
		writeBytes(file, bytes.getBytes(UTF_8));
	}

	public void writeBytes(final File file, final byte[] bytes) {
		Future<Boolean> future = Async.async(() -> {
			try {
				Files.write(file.toPath(), bytes);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return Boolean.FALSE;
		});
		get().futureManager().addFuture(future);
	}

	public Config getConfig(final String alias) {
		if (!directory.equals(saved)) throw new SeriexException("This exception should NEVER throw, unless you or I fucked something up.");
		return this.fileVariablesMap.get(alias).item2();
	}

	public File getFile(final String alias) {
		if (!directory.equals(saved)) throw new SeriexException("This exception should NEVER throw, unless you or I fucked something up.");
		return this.fileVariablesMap.get(alias).item1();
	}

	public void validateFile(File file, Config config) {
		if (file == null) {
			logger().fatal("There is no file to validate.");
		} else {
			boolean isFileValid = true;
			boolean exception = false;
			byte[] readAllBytes = null;
			try {
				readAllBytes = Files.readAllBytes(file.toPath());
				isFileValid = readAllBytes.length > 2;
			}
			catch (Exception e) {
				exception = true;
				e.printStackTrace();
				logger().fatal("Couldnt validate file! (%s)", e.getMessage());
			}
			if (!isFileValid && !config.manual) {
				logger().info("File %s was not valid!", file.getName());
				config.loadDefaultValues();
				config.save();
				config.load();
			} else if (isFileValid && !exception) {
				Seriex.logger().debug("byte length of %s : %s", config.name, readAllBytes == null ? -1 : readAllBytes.length);
			}
		}
	}

	public void createFile(Map<String, Boolean> map, String alias, Config config) {
		boolean multi = config != null && config.hasMultiple();
		File file = new File(directory, alias + (multi ? "" : EXTENSION));
		if (!multi && config != null) {
			config.toWrite = file;
		}
		boolean createFile0 = createFile0(alias, file, config);
		map.put(alias, createFile0);
	}

	private boolean createFile0(final String alias, final File file, Config config) {
		try {
			boolean megaRetardMode = false;
			boolean isDirectory = "".equals(getExtension(file.getName()));
			if (megaRetardMode) {
				logger().info("%s for %s (path %s, config %s)", String.format("Creating %s", isDirectory ? "directory" : "file"), alias, file.toPath().toAbsolutePath().toString(),
							config == null ? "no config [directory]" : config.name);
			}
			boolean created = isDirectory ? file.mkdirs() : file.createNewFile();
			if (megaRetardMode) {
				logger().info("%s for %s (path %s, config %s)",
							created ? String.format("Created %s", isDirectory ? "directory" : "file") : String.format("Couldnt create %s", isDirectory ? "directory" : "file"), alias,
							file.toPath().toAbsolutePath().toString(), config == null ? "no config [directory]" : config.name);
			}
			if (config != null) {
				if (created) {
					config.loadDefaultValues();
				}
				config.load();
			}
			Pair<File, Config> value = new Pair<>(file, config);
			boolean noIssuesSoFar = true;
			try {
				if (megaRetardMode) {
					logger().debug("Tuple value of %s -> [%s, %s]", alias, value.item1(), value.item2());
				}
				this.fileVariablesMap.put(alias, value);
				if (megaRetardMode) {
					logger().debug("Ok now getting the value of %s -> [%s, %s]", alias, getFile(alias), getConfig(alias));
				}
			}
			catch (Exception e) {
				noIssuesSoFar = false;
				e.printStackTrace();
			}
			if (noIssuesSoFar) {
				if (config != null && !isDirectory) {
					validateFile(file, config);
				}
			} else {
				logger().fatal("Couldnt validate %s (path: %s)", alias, file.toPath().toString());
			}
			return created;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}

	@Override
	public void cleanup() {
		set = false;
	}
}
