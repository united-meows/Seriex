package pisi.unitedmeows.seriex.database.structs;

import java.util.Date;

public class StructPlayerW {
    public static final String[] COLUMNS = { "player_id", "token", "pin", "salt", "last_online", "last_ip", "api_access" };

    public int player_id;
    public String token;
    public String pin;
    public String salt;
    public Date last_online;
    public String last_ip;
    public int api_access;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StructPlayerW{");
        sb.append("player_id='").append(player_id).append('\'');
        sb.append(", token='").append(token).append('\'');
        sb.append(", pin='").append(pin).append('\'');
        sb.append(", salt='").append(salt).append('\'');
        sb.append(", last_online=").append(last_online);
        sb.append(", last_ip='").append(last_ip).append('\'');
        sb.append(", api_access=").append(api_access);
        sb.append('}');
        return sb.toString();
    }
}
