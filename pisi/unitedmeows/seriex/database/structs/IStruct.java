package pisi.unitedmeows.seriex.database.structs;

import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

public interface IStruct {
	String[] getColumns();

	YSQLCommand[] setColumns();

	default void create() {
		throw new SeriexException("Override create!!");
	}

	default void update() {
		throw new SeriexException("Override update!!");
	}
}
