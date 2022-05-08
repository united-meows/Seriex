package pisi.unitedmeows.seriex.database.structs;

public class StructLastLogin {

    public static final String[] COLUMNS = { "last_login_id", "player_id", "ipaddr", "time" };

    public int player_id;
    public String ipaddr;
    public long time;
}
