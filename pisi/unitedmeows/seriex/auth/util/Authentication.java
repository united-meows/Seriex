package pisi.unitedmeows.seriex.auth.util;

import pisi.unitedmeows.seriex.auth.AuthListener;
import pisi.unitedmeows.seriex.util.inventories.LoginInventory;
import pisi.unitedmeows.seriex.util.wrapper.PlayerW;
import pisi.unitedmeows.yystal.clazz.HookClass;

public class Authentication extends HookClass<PlayerW> {
	private long startTime;

	public Authentication(PlayerW _playerW, AuthListener authListener) {
		hooked = _playerW;
		LoginInventory.open(_playerW, authListener);
	}

	public void start() {
		startTime = System.currentTimeMillis();
	}

	public void close() {}

	@Override
	public PlayerW getHooked() {
		return super.getHooked();
	}
}
