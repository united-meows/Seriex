package pisi.unitedmeows.seriex.database.structs.impl;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "player_discord")
public class StructPlayerDiscord implements IStruct {
	@Column
	public int player_discord_id;
	@Column
	public int player_id;
	@Column
	public long discord_id;
	@Column
	public String joinMS;
	@Column
	public String linkMS;
	@Column
	public String joinedAs;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}
}
