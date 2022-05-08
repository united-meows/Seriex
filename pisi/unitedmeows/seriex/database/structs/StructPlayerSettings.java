package pisi.unitedmeows.seriex.database.structs;

public class StructPlayerSettings {

    public static String[] COLUMNS = { "player_settings_id", "player_id", "ENABLE_FLAGS", "ENABLE_HUNGER",
        "ENABLE_FALLDAMAGE", "ANTICHEAT" };

    public int player_settings_id;
    public int player_id;
    public boolean ENABLE_FLAGS;
    public boolean ENABLE_HUNGER;
    public boolean ENABLE_FALLDAMAGE;

    /* TODO: maybe anticheat should be an integer? */
    public String ANTICHEAT;
}
