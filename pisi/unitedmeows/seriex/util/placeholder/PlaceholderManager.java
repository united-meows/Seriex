package pisi.unitedmeows.seriex.util.placeholder;

import java.util.HashMap;
import java.util.Map;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.managers.Manager;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class PlaceholderManager extends Manager {

    private static final HashMap<String, String> _CONSTS;

    static {
        _CONSTS = new HashMap<>();
    }

    public static String get(String input) {

        /* look for constant placeholders */
        for (Map.Entry<String, String> keyMap : _CONSTS.entrySet()) {
            input = input.replace(keyMap.getKey(), keyMap.getValue());
        }

        /* look for player based placeholders */

        return input;
    }

    @Override
    public void start(Seriex seriex) {
        /* load from config ghost */
        super.start(seriex);
    }

    @Override
    public void cleanup() throws SeriexException {
        _CONSTS.clear();
    }
}
