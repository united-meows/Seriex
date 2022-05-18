package pisi.unitedmeows.seriex.managers;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class Manager implements ICleanup {

	public void start(Seriex seriex) {}

	@Override
	public void cleanup() throws SeriexException {}
}
