package pisi.unitedmeows.seriex.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.derklaro.reflexion.Reflexion;
import net.dv8tion.jda.api.entities.UserSnowflake;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.structs.impl.player.*;
import pisi.unitedmeows.seriex.database.util.reflection.DatabaseField;
import pisi.unitedmeows.seriex.database.util.reflection.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.reflection.FieldType;
import pisi.unitedmeows.seriex.util.Parser;
import pisi.unitedmeows.seriex.util.config.single.impl.DatabaseConfig;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

public class SeriexDB extends DatabaseClient {

	public SeriexDB(DatabaseConfig config) {
		super(config.DATABASE_NAME.value());
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

	private StructPlayerWallet getPlayerWallet(YSQLCommand command) {
		return DatabaseReflection.get("player_wallet", command, this, new StructPlayerWallet());
	}

	public boolean updateStruct(IStruct struct) {
		Class<? extends IStruct> clazz = struct.getClass();
		var table = DatabaseReflection.getTable(clazz);
		var builder = new StringBuilder("UPDATE ");
		builder.append(table);
		builder.append(" SET ");
		var columnsFromClass = DatabaseReflection.getColumnsFromClass(clazz);
		var length = columnsFromClass.size();
		var discriminator = -1; // discriminator should be playerID
		var discriminatorName = "";
		for (var i = 0; i < length; i++) {
			var databaseField = columnsFromClass.get(i);

			var name = databaseField.name();
			var fieldType = databaseField.type();
			var object = Reflexion.on(struct).findField(name).orElseThrow().getValue(struct).get();
			var nullStr = "NULL";
			var valueOfField = object == null ? nullStr : object.toString();
			var isFieldString = fieldType == FieldType.STRING && !nullStr.equals(valueOfField);

			if (databaseField.discriminator()) {
				discriminatorName = databaseField.name();
				discriminator = Parser.parseInt(valueOfField, -1);
				continue;
			}

			if(databaseField.primaryKey())
				continue;

			if (!fieldType.isNullable() && nullStr.equals(valueOfField)) {
				valueOfField = nullStr;
				Seriex.get().logger().error("Non nullable field {} has a null value!", name);
			}

			if(fieldType == FieldType.BOOLEAN && object instanceof Boolean bool) {
				valueOfField = bool ? "1" : "0";
			}

			builder.append(name);
			builder.append(" = ");
			if (isFieldString) builder.append("'");
			builder.append(valueOfField);
			if (isFieldString) builder.append("'");
			if (i != length - 1) builder.append(", ");
		}

		if (discriminator >= 0) {
			builder.append(" WHERE ");
			builder.append(discriminatorName);
			builder.append(" = ");
			builder.append(discriminator);
			builder.append(";");
			execute(builder.toString().replace(",  ", " "));
			return true;
		} else {
			Seriex.get().logger().error("Discriminator could not be received!");
			return false;
		}
	}

	public boolean createStruct(IStruct struct) {
		Class<? extends IStruct> clazz = struct.getClass();
		var table = DatabaseReflection.getTable(clazz);
		var builder = new StringBuilder(String.format("INSERT INTO %s", table));
		// INSERT INTO player
		builder.append("(");
		// INSERT INTO player(
		var columnsFromClass = DatabaseReflection.getColumnsFromClass(clazz);
		var size = columnsFromClass.size();
		for (var i = 0; i < size; i++) {
			DatabaseField databaseField = columnsFromClass.get(i);
			if(databaseField.primaryKey()) continue;
			var name = databaseField.name();
			if (i == size - 1) builder.append(name).append(") ");
			else builder.append(name).append(", ");
		}
		// INSERT INTO player(api_access, username, password, token, gAuth, salt, ...)
		builder.append("VALUES(");
		final var nullStr = "NULL";
		for (var j = 0; j < size; j++) {
			var databaseField = columnsFromClass.get(j);
			var columnName = databaseField.name();
			var fieldType = databaseField.type();
			if(databaseField.primaryKey()) continue;

			var object = Reflexion.on(struct).findField(columnName).orElseThrow().getValue(struct).get();
			var valueOfField = object == null ? nullStr : object.toString();
			if (!fieldType.isNullable() && nullStr.equals(valueOfField)) {
				valueOfField = nullStr;
				Seriex.get().logger().error("Non nullable field {} has a null value!", columnName);
			}

			if(fieldType == FieldType.BOOLEAN && object instanceof Boolean bool) {
				valueOfField = bool ? "1" : "0";
			}

			var isFieldString = fieldType == FieldType.STRING && !nullStr.equals(valueOfField);
			if (isFieldString) builder.append("'");
			builder.append(valueOfField);
			if (isFieldString) builder.append("'");
			if (j == size - 1) builder.append(")");
			else builder.append(", ");
		}
		// INSERT INTO player(api_access, username, password, token, gAuth, salt)
		// VALUES(31, "probablyThisDoesntwork", "pass", "tokenqwe", "gAuthEX", "saltEx")
		var build = builder.toString();
		var execute = execute(new YSQLCommand(build));
		if (!execute)
			Seriex.get().logger().debug("Couldnt execute but no exceptions: {}", build);

		return true;
	}

	public StructPlayer getPlayer(String username) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE username=^ LIMIT 1").putString(username));
	}

	public StructPlayer getPlayer(int id) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE player_id=^ LIMIT 1").putInt(id));
	}

	public StructPlayerDiscord getPlayerDiscord(String username) {
		var pID = getPlayer(username).player_id;
		return getPlayerDiscord(pID);
	}

	public StructPlayerDiscord getPlayerDiscord(int pID) {
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE player_id=^ LIMIT 1").putInt(pID));
	}

	public List<StructPlayer> getPlayers() {
		var cmd = new YSQLCommand("SELECT * FROM player");
		List<StructPlayer> players = new ArrayList<>();
		try (var command = this.connection.prepareStatement(cmd.getHooked())) {
			command.execute();
			try (var resultSet = command.getResultSet()) {
				while (resultSet.next()) {
					var player_id = resultSet.getInt("player_id");
					var structPlayer = getPlayer(player_id);
					players.add(structPlayer);
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}

	public List<StructPlayerDiscord> getPlayerDiscordAccounts(long discord_id) {
		var cmd = new YSQLCommand("SELECT * FROM player_discord WHERE snowflake=^").putRaw(discord_id);
		List<StructPlayerDiscord> playerDiscords = new ArrayList<>();
		try (var command = this.connection.prepareStatement(cmd.getHooked())) {
			command.execute();
			try (var resultSet = command.getResultSet()) {
				while (resultSet.next()) {
					var player_id = resultSet.getInt("player_id");
					var playerDiscord = getPlayerDiscord(player_id);
					playerDiscords.add(playerDiscord);
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return playerDiscords;
	}

	public boolean removePlayer(int pID) {
		var tables = DatabaseReflection.tables();
		final var tableLength = tables.size();
		var success = 0;
		var playerToRemove = getPlayer(pID);
		if (playerToRemove != null) {
			var onlinePlayer = Seriex.get().plugin().getServer().getPlayerExact(playerToRemove.username);

			if(onlinePlayer != null)
				Seriex.get().kick_no_translation(onlinePlayer, "Your account has been deleted.");

			for (String table : tables) {
				var executed = execute("DELETE FROM " + table + " WHERE player_id =" + pID);
				if (executed) success++;
			}
		}
		return success == tableLength;
	}

	public StructPlayerDiscord getPlayerDiscord(UserSnowflake snowflake) {
		return getPlayerDiscord(new YSQLCommand("SELECT * FROM player_discord WHERE snowflake=^ LIMIT 1").putString(snowflake.getId()));
	}

	public StructPlayerSettings getPlayerSettings(int playerId) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id=^ LIMIT 1").putInt(playerId));
	}

	public StructPlayerWallet getPlayerWallet(int playerId) {
		return getPlayerWallet(new YSQLCommand("SELECT * FROM player_wallet WHERE player_id=^ LIMIT 1").putInt(playerId));
	}

	public StructPlayerSettings getPlayerSettings(String username) {
		return getPlayerSettings(new YSQLCommand("SELECT * FROM player_settings WHERE player_id IN (SELECT player_id FROM player WHERE username=^)").putString(username));
	}

	public List<StructPlayerLogin> getPlayerLogins(int playerId) {
		var cmd = new YSQLCommand("SELECT * FROM player_login WHERE player_id=^").putInt(playerId);
		List<StructPlayerLogin> lastLogins = new ArrayList<>();
		try (var command = this.connection.prepareStatement(cmd.getHooked())) {
			command.execute();
			try (var resultSet = command.getResultSet()) {
				while (resultSet.next()) {
					var structPlayerLogin = new StructPlayerLogin();
					structPlayerLogin.player_login_id = resultSet.getInt("player_login_id");
					structPlayerLogin.player_id = resultSet.getInt("player_id");
					structPlayerLogin.ms = resultSet.getLong("ms");
					structPlayerLogin.ip_address = resultSet.getString("ip_address");

					lastLogins.add(structPlayerLogin);
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return lastLogins;
	}
}
