package pisi.unitedmeows.seriex.util;

import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

// this will work so we clean disable and re-enable seriex
public interface ICleanup { void cleanup() throws SeriexException; }
