package pisi.unitedmeows.seriex.util.config.impl.server;

import java.io.File;

import pisi.unitedmeows.seriex.util.config.impl.Config;
import pisi.unitedmeows.seriex.util.config.util.Cfg;

/**
 * example rank config:
 * [ADMIN]
 * shortcut = seriex.admin
 * coolName = "&7[&cAdmin&7]"
 * permissions = ["essentials.fly", "ataturk.permission1", "hello_i_am_a_permission.doCringe"]
 */
@Cfg(name = "Ranks" , manual = true , multi = false)
public class RanksConfig extends Config {
	public RanksConfig(File toWrite) {
		super("Ranks", true);
		this.toWrite = toWrite;
	}

	@Override
	public void load() {
		internalLoad(this);
	}

	@Override
	public void loadDefaultValues() {
		internalDefaultValues(this);
	}
}
