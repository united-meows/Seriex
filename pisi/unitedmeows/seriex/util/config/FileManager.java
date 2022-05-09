package pisi.unitedmeows.seriex.util.config;

import static java.nio.charset.StandardCharsets.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.impl.server.PlayerConfig;
import pisi.unitedmeows.seriex.util.config.impl.server.ServerConfig;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.parallel.Async;
import pisi.unitedmeows.yystal.parallel.Future;
import pisi.unitedmeows.yystal.utils.Pair;

public class FileManager implements ICleanup {
	public static final String USERS = "users";
	public static final String SUB_USERS = "sub_users";
	public static final String EMOTES = "global_emotes";
	public static final String SETTINGS = "settings";
	public static final String EXTENSION = ".seriex";
	public static final String PRIVATE = "#PRIVATE#";
	private final Map<String, Pair<File, Config>> fileVariablesMap = new HashMap<>();
	private final Map<String, Pair<File, PlayerConfig>> userFiles = new HashMap<>();
	public static File directory , saved;
	public static boolean set;

	public FileManager(File pluginDirectory) {
		this.directory = pluginDirectory;
		if (!set) {
			this.saved = pluginDirectory;
			//			TODO hypixel like emotes system
			//			like sending <3 will make it this -> ♡ or ♥ idk LOL
			//			this.createFile(EMOTES, new File(directory, EMOTES + EXTENSION), null);
			this.createFile(USERS, new File(directory, USERS), null);
			File file = new File(directory, SETTINGS + EXTENSION);
			this.createFile(SETTINGS, file, new ServerConfig(file));
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
		Seriex.get().futureManager().addFuture(future);
	}

	public Config getConfig(final String alias) {
		if (!directory.equals(saved)) throw new SeriexException("This exception should NEVER throw, unless you or I fucked something up.");
		return this.fileVariablesMap.get(alias).item2();
	}

	public File getFile(final String alias) {
		if (!directory.equals(saved)) throw new SeriexException("This exception should NEVER throw, unless you or I fucked something up.");
		return this.fileVariablesMap.get(alias).item1();
	}

	public Pair<File, PlayerConfig> getUserFile(String name) {
		return userFiles.get(name);
	}

	public Pair<Boolean, Pair<File, PlayerConfig>> createUser(String username) {
		try {
			Seriex.logger().info("Created PlayerConfig for %s for the database!", username);
			File file = new File(String.format("%s/%s%s", getFile(USERS), username, EXTENSION));
			PlayerConfig playerConfig = new PlayerConfig(username, file);
			Seriex.logger().info("Created the file %s for %s for the database!", file.toPath().toString(), username);
			boolean created = createFile("user_" + username, file, playerConfig);
			Seriex.logger().info("%s!", created ? "The files have been succesfully created!" : "Files couldnt be created?");
			Pair<File, PlayerConfig> tuple = userFiles.computeIfAbsent(username, (String username_) -> new Pair<File, PlayerConfig>(file, playerConfig));
			Seriex.logger().info("%s was put into the file cache!");
			return new Pair<>(created, tuple);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void validateFile(File file, Config config) {
		if (file == null) {
			Seriex.logger().fatal("There is no file to validate.");
		} else {
			boolean isFileValid = true;
			try {
				isFileValid = Files.readAllBytes(file.toPath()).length != 0;
			}
			catch (Exception e) {
				Seriex.logger().fatal("Couldnt validate file");
			}
			if (!isFileValid) {
				Seriex.logger().info("File %s was not valid!", file.getName());
				config.loadDefaultValues();
				config.save();
				config.load();
			}
		}
	}

	public boolean createFile(final String alias, final File file, Config config) {
		try {
			boolean isDirectory = "".equals(FilenameUtils.getExtension(file.getName()));
			Seriex.logger().info("%s for %s (path %s, config %s)", String.format("Creating %s", isDirectory ? "directory" : "file"), alias, file.toPath().toAbsolutePath().toString(),
						config == null ? "no config [directory]" : config.name());
			boolean created = isDirectory ? file.mkdirs() : file.createNewFile();
			Seriex.logger().info("%s for %s (path %s, config %s)",
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
				Seriex.logger().debug("Tuple value of %s -> [%s, %s]", alias, value.item1(), value.item2());
				this.fileVariablesMap.put(alias, value);
				Seriex.logger().debug("Ok now getting the value of %s", alias);
				Seriex.logger().debug("Tuple value of %s -> [%s, %s]", alias, getFile(alias), getConfig(alias));
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
				Seriex.logger().fatal("Couldnt ");
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
