package test.impl;

import static pisi.unitedmeows.seriex.database.util.DatabaseReflection.init;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.electronwill.nightconfig.core.file.FileConfig;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.util.math.Hashing;
import pisi.unitedmeows.seriex.util.timings.TimingsCalculator;
import pisi.unitedmeows.seriex.util.web.ConnectionUtils;
import test.Test;
import test.TestSettings;
import test.TestState;

@TestSettings(hasArguments = false)
public class DatabaseTest extends Test {
	private AtomicBoolean noPlayerFound = new AtomicBoolean();
	private AtomicReference<StructPlayer> atomicPlayer = new AtomicReference<>();

	@Override
	public TestState run() {
		try {
			File file = testFile("\\database\\config.toml");
			if (!file.exists()) {
				boolean created = file.createNewFile();
				if (!created) {
					message("Couldnt create config.toml file for Database Test!");
					return TestState.FATAL_ERROR;
				}
				message("Database Test doesnt have a config file... generated one!", "Fill the config file and try running tests again.");
				return TestState.WARNING;
			}
			if (!ConnectionUtils.hasInternetConnection()) {
				message("No internet connection.", "Do you have a connection to TOR/Loki/VPN?");
				return TestState.FATAL_ERROR;
			}
			try (FileConfig fileConfig = FileConfig.of(file)) {
				fileConfig.load();
				String username = fileConfig.get("username");
				String password = fileConfig.get("password");
				String databaseName = fileConfig.get("database");
				String host = fileConfig.get("host");
				int port = fileConfig.get("port");
				boolean create = fileConfig.get("create");
				SeriexDB database = new SeriexDB(username, password, databaseName, host, port);
				if (!database.connected()) {
					Seriex.logger().fatal("Couldnt connect to the database!");
					return TestState.FATAL_ERROR;
				}
				TimingsCalculator.GET.benchmark(() -> {
					init(database);
				}, "Database using Reflection");
				TimingsCalculator.GET.benchmark(() -> {
					StructPlayer player = database.getPlayer("ghost2173");
					if (player == null) {
						noPlayerFound.set(true);
						Seriex.logger().fatal("No player found... maybe database doesnt have one?");
					} else {
						Seriex.logger().debug("Found player! Information about the player: %s\n", player.toString());
					}
				}, "Getting player 'ghost2173'");
				if (!create) {
					boolean noPlayer = noPlayerFound.get();
					if (noPlayer) {
						message("Database doesn't have the player 'ghost2173' or the database is corrupt!");
						return TestState.WARNING;
					} else return TestState.SUCCESS;
				} else {
					String tempPlayerUsername = "tempUser_" + System.currentTimeMillis();
					try {
						TimingsCalculator.GET.benchmarkc(() -> {
							StructPlayer structPlayer = new StructPlayer();
							structPlayer.username = tempPlayerUsername;
							structPlayer.salt = Hashing.randomString(8);
							structPlayer.firstLogin = true;
							structPlayer.password = Hashing.hashedString(structPlayer.salt + "lololo");
							structPlayer.create(database);
						}, "User creation took %s ms");
					}
					catch (Exception e) {
						Seriex.logger().fatal("Couldnt create user!");
						return TestState.FAIL;
					}
					try {
						TimingsCalculator.GET.benchmarkc(() -> {
							atomicPlayer.set(database.getPlayer(tempPlayerUsername));
						}, "Getting player took %s ms");
					}
					catch (Exception e) {
						Seriex.logger().fatal("Couldnt get user!");
						return TestState.FAIL;
					}
					StructPlayer structPlayer = atomicPlayer.get();
					if (structPlayer == null) {
						message("Couldnt get player after creation!", "POSSIBLE FIXES:", "Probably cannot connect to the database",
									"Check your config file for any typos in username/password/host/port etc.");
						return TestState.FATAL_ERROR;
					} else {
						Seriex.logger().debug("Found player! Information about the player: %s\n", structPlayer.toString());
						TimingsCalculator.GET.benchmarkc(() -> {
							StructPlayer modify = database.getPlayer(username);
							modify.banned = true;
							modify.update(database);
							Seriex.logger().debug("Modified player! Information about the previous player: %s\n Information about the modified player: %s\n", structPlayer.toString(), modify.toString());
						}, "Modifying the player took %s ms");
						return TestState.SUCCESS;
					}
				}
			}
			catch (Exception e) {
				message(e);
				return TestState.FATAL_ERROR;
			}
		}
		catch (Exception e) {
			message(e);
			return TestState.FATAL_ERROR;
		}
	}
}
