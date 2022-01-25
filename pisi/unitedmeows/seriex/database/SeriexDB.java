package pisi.unitedmeows.seriex.database;

import pisi.unitedmeows.yystal.sql.YDatabaseClient;

public class SeriexDB extends YDatabaseClient {

	/* get from config */
	public SeriexDB(String username, String password, String database, String host, int port) {
		super(username, password, database, host, port);
	}
}
