package pisi.unitedmeows.seriex.database.structs;

import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public interface IStruct {
	default boolean create() {
		throw SeriexException.create("Override create!");
	}

	default boolean update() {
		throw SeriexException.create("Override update!");
	}
}
