package pisi.unitedmeows.seriex.config;

import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

import java.util.List;
import java.util.Map;

public class DatabaseDataProvider implements IDataProvider {

	protected YDatabaseClient client;

	public DatabaseDataProvider(YDatabaseClient _databaseClient) {
		client = _databaseClient;
	}

	@Override
	public PlayerConfig playerConfig(final String username) {
		return playerConfigFromSql(new YSQLCommand("SELECT * FROM playerw WHERE username=^ LIMIT 1")
				.putString(username));
	}

	protected PlayerConfig playerConfigFromSql(YSQLCommand ysqlCommand) {
		// tries to retrieve player from database
		List<Map<String, Object>> resultList = client.select(ysqlCommand,
				"username", "token");


		System.out.println(resultList);
		/* return null if empty */
		if (resultList.isEmpty())
			return null;

		/* get first result */
		Map<String, Object> result = resultList.get(0);


		/* fill the config with the db values */
		PlayerConfig playerConfig = new PlayerConfig();
		playerConfig.token = (String) result.get("token");
		playerConfig.username = (String) result.get("username");

		return playerConfig;
	}


}
