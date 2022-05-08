package pisi.unitedmeows.seriex.database;

import pisi.unitedmeows.seriex.database.structs.StructLastLogin;
import pisi.unitedmeows.seriex.database.structs.StructPlayerSettings;
import pisi.unitedmeows.seriex.database.structs.StructPlayerW;
import pisi.unitedmeows.yystal.sql.YDatabaseClient;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//TODO: find better way to fill values (annotations) instead of doing it manually @ghost
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
            return null;
        }

        StructPlayerW structPlayerW = new StructPlayerW();
        structPlayerW.player_id = (int) query.get(0).get("player_id");
        structPlayerW.token = (String) query.get(0).get("token");
        structPlayerW.pin = (String) query.get(0).get("pin");
        structPlayerW.salt = (String) query.get(0).get("salt");
        structPlayerW.last_online = (Date) query.get(0).get("last_online");
        structPlayerW.last_ip = (String) query.get(0).get("last_ip");
        structPlayerW.api_access = (Integer) query.get(0).get("api_access");
        return structPlayerW;
    }

    public StructPlayerW getPlayerW(String username) {
        return getPlayerW( new YSQLCommand("SELECT * FROM playerw WHERE username=^ LIMIT 1").putString(username));
    }

    public StructPlayerW getPlayerWFromToken(String token) {
        return getPlayerW( new YSQLCommand("SELECT * FROM playerw WHERE token=^ LIMIT 1").putString(token));
    }

    public StructPlayerSettings getPlayerSetting(YSQLCommand command) {
        List<Map<String, Object>> query = select(command, StructPlayerSettings.COLUMNS);
        if (query.isEmpty()) {
            return null;
        }

        StructPlayerSettings structPlayerSetting = new StructPlayerSettings();
        structPlayerSetting.player_settings_id = (int) query.get(0).get("player_settings_id");
        structPlayerSetting.player_id = (int) query.get(0).get("player_id");
        structPlayerSetting.ENABLE_FLAGS = (boolean) query.get(0).get("ENABLE_FLAGS");
        structPlayerSetting.ENABLE_HUNGER = (boolean) query.get(0).get("ENABLE_HUNGER");
        structPlayerSetting.ENABLE_FALLDAMAGE = (boolean) query.get(0).get("ENABLE_FALLDAMAGE");
        structPlayerSetting.ANTICHEAT = (String) query.get(0).get("ANTICHEAT");

        return structPlayerSetting;
    }

    public StructPlayerSettings getPlayerSetting(int playerId) {
        return getPlayerSetting(new YSQLCommand("SELECT * FROM player_settings WHERE player_id=^ LIMIT 1").putInt(playerId));
    }

    /* this method uses double query and can be slower compared than playerId */
    /* avoid using this when you have the playerId */
    public StructPlayerSettings getPlayerSetting(String username) {
        return getPlayerSetting(new YSQLCommand(
                "SELECT * FROM player_settings WHERE player_id IN (SELECT player_id FROM playerw WHERE username=^)")
                .putString(username));
    }

    public boolean createPlayerSettings(StructPlayerSettings playerSettings) {
        return execute(new YSQLCommand(
    "INSERT INTO player_settings (player_id, ENABLE_FLAGS, ENABLE_HUNGER, ENABLE_FALLDAMAGE, ANTICHEAT) VALUES (^, ^, ^, ^, ^)")
                .putInt(playerSettings.player_id)
                .putBool(playerSettings.ENABLE_FLAGS)
                .putBool(playerSettings.ENABLE_HUNGER)
                .putBool(playerSettings.ENABLE_FALLDAMAGE)
                .putString(playerSettings.ANTICHEAT));
    }

    public List<StructLastLogin> getLastLogins(YSQLCommand command) {
        List<Map<String, Object>> query = select(command, StructPlayerSettings.COLUMNS);
        if (query.isEmpty()) {
            return new ArrayList<>();
        }

        List<StructLastLogin> lastLogins = new ArrayList<>();
        for (Map<String, Object> lastLoginMap : query) {
            StructLastLogin lastLogin = new StructLastLogin();
            lastLogin.time = (long) lastLoginMap.get("time");
            lastLogin.player_id = (int) lastLoginMap.get("player_id");
            lastLogin.ipaddr = (String) lastLoginMap.get("ipaddr");
            lastLogins.add(lastLogin);
        }

        return lastLogins;
    }

    public List<StructLastLogin> getLastLogins(int playerId, int max) {
        return getLastLogins(new YSQLCommand("SELECT * FROM last_logins WHERE player_id=^ ORDER BY time DESC LIMIT ^")
                .putInt(playerId)
                .putInt(max));
    }

    public StructLastLogin getLastLogin(int playerId) {
        List<StructLastLogin> lastLogins = getLastLogins(playerId, 1);
        if (lastLogins.isEmpty()) {
            return null;
        }
        return lastLogins.get(0);
    }

    /* get lastlogins with playerid if you can */
    /* this method uses double query */
    public List<StructLastLogin> getLastLogins(String username, int max) {
        return getLastLogins(new YSQLCommand("SELECT * FROM last_logins WHERE player_id IN (SELECT player_id FROM playerw WHERE username=^) ORDER BY time DESC LIMIT ^")
                .putString(username)
                .putInt(max));
    }

    public StructLastLogin getLastLogin(String username) {
        List<StructLastLogin> lastLogins = getLastLogins(username, 1);
        if (lastLogins.isEmpty()) {
            return null;
        }
        return lastLogins.get(0);
    }

    public boolean createLastLogin(int playerId, String ip) {
        return execute(new YSQLCommand("INSERT INTO last_logins (player_id, ipaddr, time) VALUES (^, ^, ^)")
                .putInt(playerId)
                .putString(ip)
                .putRaw(System.currentTimeMillis()));
    }


}
