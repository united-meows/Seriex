package pisi.unitedmeows.seriex.database;

import pisi.unitedmeows.seriex.database.structs.impl.*;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

public class SeriexDB extends YDatabaseClient {
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

	// TODO get the class from reflections maybe...
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

	public StructPlayer getPlayer(String username) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE username=^ LIMIT 1").putString(username));
	}

	public StructPlayer getPlayerFromToken(String token) {
		return getPlayer(new YSQLCommand("SELECT * FROM player WHERE token=^ LIMIT 1").putString(token));
	}
}
