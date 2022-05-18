package pisi.unitedmeows.seriex.database.structs.impl.areas;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "area")
public class StructArea implements IStruct {
	@Column
	public int area_id;
	@Column
	public int api_access;
	@Column
	public String username;
	@Column
	public String password;
	@Column
	public String token;
	@Column
	public String gAuth;
	@Column
	public String salt;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}
}
