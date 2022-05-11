package pisi.unitedmeows.seriex.util.config;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.FilenameUtils.*;
import static pisi.unitedmeows.seriex.Seriex.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.TranslationsConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.WorldConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;
import pisi.unitedmeows.yystal.utils.Pair;

// TODO alo ghost refactor this code sucks ass
public class FileManager extends Manager {
	public static final String EMOTES = "global_emotes";
	public static final String SETTINGS = "settings";
	public static final String WORLD = "world_";
	public static final String TRANSLATIONS = "translations";
	public static final String EXTENSION = ".seriex";
	public static final String PRIVATE = "#PRIVATE#";
	private final Map<String, Pair<File, Config>> fileVariablesMap = new HashMap<>();
	public static File directory , saved;
	public static boolean set;

	public FileManager(File pluginDirectory) {
		this.directory = pluginDirectory;
		if (!set) {
			this.saved = pluginDirectory;
			get().getServer().getWorlds().forEach(world -> 
						this.createFile(String.format("%s%s", WORLD, world.getName()), 
						new File(String.format("%s/worlds", directory)),
						new WorldConfig(world.getName(), EXTENSION, directory)));
			File settingsFile = new File(directory, SETTINGS + EXTENSION);
			this.createFile(SETTINGS, settingsFile, new ServerConfig(settingsFile));
			File translationsFile = new File(directory, TRANSLATIONS + EXTENSION);
			this.createFile(TRANSLATIONS, translationsFile, new TranslationsConfig(translationsFile));
			set = true;
		}
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
