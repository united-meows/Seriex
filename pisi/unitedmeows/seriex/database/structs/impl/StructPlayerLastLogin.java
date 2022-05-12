package pisi.unitedmeows.seriex.database.structs.impl;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "player_last_login")
public class StructPlayerLastLogin implements IStruct {
	@Column
	public int player_last_login_id;
	@Column
	public int player_id;
	@Column
	public String ip_adress;
	@Column
	public String date;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}

	@Override
	public String toString() {
		return String.format("StructPlayerLastLogin [player_last_login_id=%s, player_id=%s, ip_adress=%s, date=%s]", player_last_login_id, player_id, ip_adress, date);
	}
}
