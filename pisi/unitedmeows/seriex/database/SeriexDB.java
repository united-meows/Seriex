package pisi.unitedmeows.seriex.database;

import pisi.unitedmeows.seriex.database.structs.StructPlayerW;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    public StructPlayerW getPlayerW(YSQLCommand command) {
        List<Map<String, Object>> query = select(command, StructPlayerW.COLUMNS);
        if (query.isEmpty()) {
            return new StructPlayerW();
        }

        StructPlayerW structPlayerW = new StructPlayerW();
        structPlayerW.player_id = (int) query.get(0).get("player_id");
        structPlayerW.token = (String) query.get(0).get("token");
        structPlayerW.pin = (String) query.get(0).get("pin");
        structPlayerW.salt = (String) query.get(0).get("salt");
        structPlayerW.last_online = (Date) query.get(0).get("last_online");
        structPlayerW.last_ip = (String) query.get(0).get("last_ip");
        structPlayerW.api_access = (Integer) query.get(0).get("api_access");
        structPlayerW.isNew = false;
        return structPlayerW;
    }

    public StructPlayerW getPlayerW(String username) {
        return getPlayerW( new YSQLCommand("SELECT * FROM playerw WHERE username=^ LIMIT 1").putString(username));
    }

    public StructPlayerW getPlayerWFromToken(String token) {
        return getPlayerW( new YSQLCommand("SELECT * FROM playerw WHERE token=^ LIMIT 1").putString(token));
    }



}
