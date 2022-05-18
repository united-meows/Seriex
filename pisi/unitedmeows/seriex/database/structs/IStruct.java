package pisi.unitedmeows.seriex.database.structs;

import pisi.unitedmeows.yystal.sql.YSQLCommand;

public interface IStruct {

	String[] getColumns();

	YSQLCommand[] setColumns();

	default void create() {}
}
