package pisi.unitedmeows.seriex.util.config;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.FilenameUtils.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.managers.area.areas.Area;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.server.*;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;
import pisi.unitedmeows.yystal.utils.Pair;

public class FileManager extends Manager {
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
	public static final String EXTENSION = ".seriex";
	public static final String PRIVATE = "#PRIVATE#";
	public static final String AUTH = "AUTH";
	private final Map<String, Pair<File, Config>> fileVariablesMap = new HashMap<>();
	public static File directory , saved;
	public static boolean set;

	public FileManager(File pluginDirectory) {
		this.directory = pluginDirectory;
		if (!set) {
			this.saved = pluginDirectory;
			File settingsFile = new File(directory, SERVER + EXTENSION);
			File translationsFile = new File(directory, TRANSLATIONS + EXTENSION);
			File banActionsFile = new File(directory, BAN_ACTIONS + EXTENSION);
			File maintainersFile = new File(directory, MAINTAINERS + EXTENSION);
			File ranksFile = new File(directory, RANKS + EXTENSION);
			File databaseFile = new File(directory, DATABASE + EXTENSION);
			File discordFile = new File(directory, DISCORD + EXTENSION);
			File worldDirectory = new File(directory, WORLD);
			File authFile = new File(directory, AUTH + EXTENSION);
			if (Seriex.available()) {
				this.createFile(WORLD, worldDirectory, new WorldConfig(worldDirectory, EXTENSION, get().getServer().getWorlds().stream().toArray(World[]::new)));
			}
			this.createFile(DISCORD, discordFile, new DiscordConfig(discordFile));
			this.createFile(DATABASE, databaseFile, new DatabaseConfig(databaseFile));
			this.createFile(AUTH, authFile, new AuthConfig(authFile));
			this.createFile(BAN_ACTIONS, banActionsFile, new BanActionsConfig(banActionsFile));
			this.createFile(RANKS, ranksFile, new RanksConfig(ranksFile));
			this.createFile(MAINTAINERS, maintainersFile, new MaintainersConfig(maintainersFile));
			this.createFile(SERVER, settingsFile, new ServerConfig(settingsFile));
			//			this.createFile(TRANSLATIONS, translationsFile, new TranslationsConfig(translationsFile));
			set = true;
		}
	}

	@Override
	public void post(Seriex seriex) {
		File areasDirectory = new File(directory, WORLD);
		this.createFile(AREAS, areasDirectory, new AreaConfig(areasDirectory, EXTENSION, get().areaManager().areaList.stream().toArray(Area[]::new)));
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
			try {
				isFileValid = Files.readAllBytes(file.toPath()).length > 2;
			}
			catch (Exception e) {
				e.printStackTrace();
				logger().fatal("Couldnt validate file! (%s)", e.getMessage());
			}
			if (!isFileValid && !config.manual) {
				logger().info("File %s was not valid!", file.getName());
				config.loadDefaultValues();
				config.save();
				config.load();
			}
		}
	}

	public boolean createFile(final String alias, final File file, Config config) {
		try {
			boolean isDirectory = "".equals(getExtension(file.getName()));
			logger().info("%s for %s (path %s, config %s)", String.format("Creating %s", isDirectory ? "directory" : "file"), alias, file.toPath().toAbsolutePath().toString(),
						config == null ? "no config [directory]" : config.name());
			boolean created = isDirectory ? file.mkdirs() : file.createNewFile();
			logger().info("%s for %s (path %s, config %s)",
						created ? String.format("Created %s", isDirectory ? "directory" : "file") : String.format("Couldnt create %s", isDirectory ? "directory" : "file"), alias,
						file.toPath().toAbsolutePath().toString(), config == null ? "no config [directory]" : config.name());
			if (config != null) {
				if (created) {
					config.loadDefaultValues();
				}
				config.load();
			}
			Pair<File, Config> value = new Pair<>(file, config);
			boolean noIssuesSoFar = true;
			try {
				logger().debug("Tuple value of %s -> [%s, %s]", alias, value.item1(), value.item2());
				this.fileVariablesMap.put(alias, value);
				logger().debug("Ok now getting the value of %s -> [%s, %s]", alias, getFile(alias), getConfig(alias));
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
