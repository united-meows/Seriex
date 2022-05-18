package pisi.unitedmeows.seriex.database;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			StringBuilder builder = new StringBuilder(String.format("INSERT INTO %s", DatabaseReflection.getTable(clazz)));
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
				}
				if (j == length - 1) {
					builder.append(String.format("%s)", valueOfField.toString()));
				} else {
					builder.append(String.format("%s, ", valueOfField.toString()));
				}
			}
			// INSERT INTO player(player_id, api_access, username, password, token, gAuth, salt)
			// VALUES(1, 31, "probablyThisDoesntwork", "pass", "tokenqwe", "gAuthEX", "saltEx")
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

	public StructPlayer getPlayerFromToken(String token) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE token=^ LIMIT 1").putString(token));
	}

	public StructPlayerSettings getPlayerSettings(int playerId) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id=^ LIMIT 1").putInt(playerId));
	}

	public StructPlayerSettings getPlayerSettings(String username) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id IN (SELECT player_id FROM playerw WHERE username=^)").putString(username));
	}

	public StructPlayerLastLogin getLastLogin(int playerId) {
		return getPlayerLastLogin(new YSQLCommand("SELECT * FROM player_last_login WHERE player_id=^ ORDER BY time DESC LIMIT 1").putInt(playerId));
	}

	public StructPlayerLastLogin getLastLogin(String username) {
		return getPlayerLastLogin(new YSQLCommand("SELECT * FROM player_last_login WHERE player_id IN (SELECT player_id FROM player WHERE username=^) ORDER BY time DESC LIMIT 1").putString(username));
	}

	@Override
	public void cleanup() throws SeriexException {
		DatabaseReflection.cleanup();
	}
}
