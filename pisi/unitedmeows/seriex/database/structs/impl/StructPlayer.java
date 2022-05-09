package pisi.unitedmeows.seriex.database.structs.impl;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "player")
public class StructPlayer implements IStruct {
	@Column
	public int player_id;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StructPlayer [player_id=").append(player_id).append(", api_access=").append(api_access).append(", username=").append(username).append(", password=").append(password)
					.append(", token=").append(token).append(", gAuth=").append(gAuth).append(", salt=").append(salt).append("]");
		return builder.toString();
	}
}
