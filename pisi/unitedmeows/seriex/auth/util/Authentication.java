package pisi.unitedmeows.seriex.auth.util;

import io.netty.util.concurrent.Promise;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.clazz.HookClass;

public class Authentication extends HookClass<PlayerW> {

    private long startTime;

    public Authentication(PlayerW _playerW) {
        hooked = _playerW;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void close() {

    }

    @Override
    public PlayerW getHooked() {
        return super.getHooked();
    }
}
