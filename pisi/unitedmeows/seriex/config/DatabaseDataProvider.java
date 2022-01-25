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

	@Override
	public void createPlayerConfig(String username, PlayerConfig config) {
		YSQLCommand ysqlCommand = new YSQLCommand(
				"INSERT INTO playerw(username, address, token) VALUES (^, ^, ^)")
				.putString(config.username)
				.putString(config.address)
				.putString(config.token);

		System.out.println(ysqlCommand.toString());

		client.execute(ysqlCommand);
	}

	@Override
	public void deletePlayerConfig(String username) {
		client.execute(new YSQLCommand("DELETE FROM playerw WHERE username=^")
				.putString(username));
	}

	protected PlayerConfig playerConfigFromSql(YSQLCommand ysqlCommand) {
		// tries to retrieve player from database
		List<Map<String, Object>> resultList = client.select(ysqlCommand,
				"username", "address", "token");


		System.out.println(resultList);
		/* return null if empty */
		if (resultList.isEmpty())
			return null;

		/* get first result */
		Map<String, Object> result = resultList.get(0);


		/* fill the config with the db values */
		PlayerConfig playerConfig = new PlayerConfig();
		playerConfig.token = (String) result.get("token");
		playerConfig.address = (String) result.get("address");
		playerConfig.username = (String) result.get("username");

		return playerConfig;
	}

}
