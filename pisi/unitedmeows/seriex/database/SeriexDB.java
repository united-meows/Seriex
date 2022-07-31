package pisi.unitedmeows.seriex.database;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayer;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerDiscord;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerFirstLogin;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerLastLogin;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerSettings;
import pisi.unitedmeows.seriex.database.structs.impl.player.StructPlayerWallet;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection.FieldType;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;
import pisi.unitedmeows.yystal.utils.Pair;

public class SeriexDB extends YDatabaseClient implements ICleanup {
	public SeriexDB(String username, String password, String database, String host, int port) {
		super(username, password, database, host, port);
	}

	public SeriexDB(String username, String password, String database, String host) {
		super(username, password, database, host);
	}

	public SeriexDB(String username, String password, String database) {
		super(username, password, database);
	}

	private StructPlayer getPlayer(YSQLCommand command) {
		return DatabaseReflection.get("player", command, this, new StructPlayer());
	}

	private StructPlayerSettings getPlayerSettings(YSQLCommand command) {
		return DatabaseReflection.get("player_settings", command, this, new StructPlayerSettings());
	}

	private StructPlayerDiscord getPlayerDiscord(YSQLCommand command) {
		return DatabaseReflection.get("player_discord", command, this, new StructPlayerDiscord());
	}

	private StructPlayerFirstLogin getPlayerFirstLogin(YSQLCommand command) {
		return DatabaseReflection.get("player_first_login", command, this, new StructPlayerFirstLogin());
	}

	private StructPlayerLastLogin getPlayerLastLogin(YSQLCommand command) {
		return DatabaseReflection.get("player_last_login", command, this, new StructPlayerLastLogin());
	}

	private StructPlayerWallet getPlayerWallet(YSQLCommand command) {
		return DatabaseReflection.get("player_wallet", command, this, new StructPlayerWallet());
	}

	public boolean updateStruct(IStruct struct) {
		try {
			Class<? extends IStruct> clazz = struct.getClass();
			String table = DatabaseReflection.getTable(clazz);
			StringBuilder builder = new StringBuilder(String.format("UPDATE %s SET ", table));
			Pair<String[], List<Pair<String, FieldType>>> columnsFromClass = DatabaseReflection.getColumnsFromClass(clazz);
			String[] columnNames = columnsFromClass.item1();
			List<Pair<String, FieldType>> pairs = columnsFromClass.item2();
			Map<String, FieldType> unefficientCodeTime = new HashMap<>();
			pairs.forEach((Pair<String, FieldType> pair) -> unefficientCodeTime.put(pair.item1(), pair.item2()));
			int length = columnNames.length;
			int playerID = -2173;
			for (int i = 0; i < length; i++) {
				String name = columnNames[i];
				Field field = clazz.getDeclaredField(name);
				FieldType fieldType = unefficientCodeTime.get(name);
				boolean isFieldNullable = fieldType.nullable;
				Object object = field.get(struct);
				String nullStr = "NULL";
				String valueOfField = object == null ? nullStr : object.toString();
				boolean isFieldString = fieldType == FieldType.STRING && !nullStr.equals(valueOfField);
				if ("player_id".equals(name)) {
					try {
						playerID = Integer.parseInt(valueOfField);
					}
					catch (Exception e) {
						Seriex.logger().fatal("Couldnt parse player_id %s", valueOfField);
					}
				}
				if ("player_id".equals(name)) {
					continue;
				}
				if (!isFieldNullable && nullStr.equals(valueOfField)) {
					valueOfField = nullStr;
					Seriex.logger().fatal("Non nullable field %s has a null value!", name);
				}
				builder.append(name);
				builder.append(" = ");
				if (isFieldString) {
					builder.append("'");
				}
				builder.append(valueOfField);
				if (isFieldString) {
					builder.append("'");
				}
				if (i != length - 1) {
					builder.append(", ");
				}
			}
			if (playerID != -2173) {
				builder.append(" WHERE player_id = ");
				builder.append(playerID);
				builder.append(";");
				execute(builder.toString());
				return true;
			} else {
				Seriex.logger().fatal("playerID could not be received!");
				return false;
			}
		}
		catch (NoSuchFieldException
					| SecurityException
					| IllegalArgumentException
					| IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean createStruct(IStruct struct) {
		//  	based on
		//		return execute(new YSQLCommand(
		//				    "INSERT INTO player_settings "
		//				    + "(player_id, ENABLE_FLAGS, ENABLE_HUNGER, ENABLE_FALLDAMAGE, ANTICHEAT) "
		//				    + "VALUES (^, ^, ^, ^, ^)")
		//               .putInt(playerSettings.player_id)
		//               .putBool(playerSettings.ENABLE_FLAGS)
		//               .putBool(playerSettings.ENABLE_HUNGER)
		//               .putBool(playerSettings.ENABLE_FALLDAMAGE)
		//               .putString(playerSettings.ANTICHEAT));
		try {
			Class<? extends IStruct> clazz = struct.getClass();
			String table = DatabaseReflection.getTable(clazz);
			StringBuilder builder = new StringBuilder(String.format("INSERT INTO %s", table));
			// INSERT INTO player
			builder.append("(");
			// INSERT INTO player(
			Pair<String[], List<Pair<String, FieldType>>> columnsFromClass = DatabaseReflection.getColumnsFromClass(clazz);
			String[] columnNames = columnsFromClass.item1();
			List<Pair<String, FieldType>> pairs = columnsFromClass.item2();
			Map<String, FieldType> unefficientCodeTime = new HashMap<>();
			pairs.forEach((Pair<String, FieldType> pair) -> unefficientCodeTime.put(pair.item1(), pair.item2()));
			int length = columnNames.length;
			for (int i = 0; i < length; i++) {
				String name = columnNames[i];
				if (i == length - 1) {
					builder.append(name).append(") ");
				} else {
					builder.append(name).append(", ");
				}
			}
			// INSERT INTO player(player_id, api_access, username, password, token, gAuth, salt)
			builder.append("VALUES(");
			for (int j = 0; j < length; j++) {
				String columnName = columnNames[j];
				Field field = clazz.getDeclaredField(columnName);
				FieldType fieldType = unefficientCodeTime.get(columnName);
				boolean isFieldNullable = fieldType.nullable;
				Object object = field.get(struct);
				String nullStr = "NULL";
				String valueOfField = object == null ? nullStr : object.toString();
				if (!isFieldNullable && nullStr.equals(valueOfField)) {
					valueOfField = nullStr;
					Seriex.logger().fatal("Non nullable field %s has a null value!", columnName);
				}
				boolean isFieldString = fieldType == FieldType.STRING && !nullStr.equals(valueOfField);
				if (isFieldString) {
					builder.append("'");
				}
				builder.append(valueOfField);
				if (isFieldString) {
					builder.append("'");
				}
				if (j == length - 1) {
					builder.append(")");
				} else {
					builder.append(", ");
				}
			}
			// INSERT INTO player(player_id, api_access, username, password, token, gAuth, salt)
			// VALUES(1, 31, "probablyThisDoesntwork", "pass", "tokenqwe", "gAuthEX", "saltEx")
			String build = builder.toString();
			boolean execute = execute(new YSQLCommand(build));
			if (!execute) {
				Seriex.logger().debug("Couldnt execute but no exceptions...?");
				Seriex.logger().debug("Fully executed command:");
				Seriex.logger().debug(build);
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public StructPlayer getPlayer(String username) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE username=^ LIMIT 1").putString(username));
	}

	public StructPlayerDiscord getPlayerDiscord(String username) {
		int pID = getPlayer(username).player_id;
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE player_id=^ LIMIT 1").putInt(pID));
	}

	public StructPlayerDiscord getPlayerDiscord(UserSnowflake snowflake) {
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE discord_id=^ LIMIT 1").putString(snowflake.getId()));
	}

	public StructPlayer getPlayerFromToken(String token) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE token=^ LIMIT 1").putString(token));
	}

	public StructPlayerSettings getPlayerSettings(int playerId) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id=^ LIMIT 1").putInt(playerId));
	}

	public StructPlayerSettings getPlayerSettings(String username) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id IN (SELECT player_id FROM player WHERE username=^)").putString(username));
	}

	public StructPlayerLastLogin getLastLogin(int playerId) {
		return getPlayerLastLogin(new YSQLCommand("SELECT * FROM player_last_login WHERE player_last_login_id=^ ORDER BY time DESC LIMIT 1").putInt(playerId));
	}

	public StructPlayerLastLogin getLastLogin(String username) {
		return getPlayerLastLogin(
					new YSQLCommand("SELECT * FROM player_last_login WHERE player_last_login_id IN (SELECT player_id FROM player WHERE username=^) ORDER BY time DESC LIMIT 1").putString(username));
	}

	@Override
	public void cleanup() throws SeriexException {
		DatabaseReflection.cleanup();
	}
}
