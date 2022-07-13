package pisi.unitedmeows.seriex.database.structs;

import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

public interface IStruct {
	String[] getColumns();

	YSQLCommand[] setColumns();

	default boolean create() {
		throw new SeriexException("Override create!");
	}

	default boolean update() {
		throw new SeriexException("Override update!");
	}
}
