package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
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
	public void create() {
		Seriex.get().database().createStruct(this);
	}

	@Override
	public void update() {
		Seriex.get().database().updateStruct(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StructPlayerLastLogin [player_last_login_id=").append(player_last_login_id).append(", player_id=").append(player_id).append(", ip_adress=").append(ip_adress).append(", date=")
					.append(date).append("]");
		return builder.toString();
	}
}
