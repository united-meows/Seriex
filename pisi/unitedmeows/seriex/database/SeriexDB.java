package pisi.unitedmeows.seriex.database;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.structs.impl.player.*;
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
				String valueOfField = field.get(struct).toString();
				FieldType fieldType = unefficientCodeTime.get(name);
				boolean isNotNullable = !fieldType.nullable;
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
				if (isNotNullable && valueOfField == null) {
					valueOfField = "null";
					Seriex.logger().fatal("Value is not nullable but value of field is null! %s", name, fieldType.name());
				}
				boolean isFieldString = fieldType == FieldType.STRING;
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
				builder.append("WHERE player_id = ");
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

	public boolean createStruct(IStruct struct, String... extraCommands) {
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
					builder.append(name + ") ");
				} else {
					builder.append(name + ", ");
				}
			}
			// INSERT INTO player(player_id, api_access, username, password, token, gAuth, salt)
			builder.append("VALUES(");
			for (int j = 0; j < length; j++) {
				String columnName = columnNames[j];
				Field field = clazz.getDeclaredField(columnName);
				String valueOfField = field.get(struct).toString();
				if (!unefficientCodeTime.get(columnName).nullable && valueOfField == null) {
					valueOfField = "null";
					// todo fatal log
				}
				if (j == length - 1) {
					builder.append(String.format("%s)", valueOfField.toString()));
				} else {
					builder.append(String.format("%s, ", valueOfField.toString()));
				}
			}
			// INSERT INTO player(player_id, api_access, username, password, token, gAuth, salt)
			// VALUES(1, 31, "probablyThisDoesntwork", "pass", "tokenqwe", "gAuthEX", "saltEx")
			// WHERE NOT EXISTS (SELECT * FROM player WHERE username='username') <- extra command usually
			if (extraCommands.length != 0) {
				builder.append(String.format(extraCommands[0], table));
			}
			return execute(new YSQLCommand(builder.toString()));
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
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE player_discord_id=^ LIMIT 1").putString(username));
	}

	public StructPlayerDiscord getPlayerDiscord(UserSnowflake snowflake) {
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE discord_id=^ LIMIT 1").putString(snowflake.getId()));
	}

	public StructPlayer getPlayerFromToken(String token) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE token=^ LIMIT 1").putString(token));
	}

	public StructPlayerSettings getPlayerSettings(int playerId) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_settings_id=^ LIMIT 1").putInt(playerId));
	}

	public StructPlayerSettings getPlayerSettings(String username) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_settings_id IN (SELECT player_id FROM playerw WHERE username=^)").putString(username));
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
