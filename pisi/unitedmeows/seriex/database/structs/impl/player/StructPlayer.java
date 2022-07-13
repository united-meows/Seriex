package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
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
	@Column
	public boolean banned;
	@Column
	public boolean firstLogin;
	@Column
	public int timesLogined;
	@Column
	public long playTime;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}

	/**
	 * testing uses only
	 */
	@Deprecated
	public void create(SeriexDB db) {
		db.createStruct(this);
	}

	/**
	 * testing uses only
	 */
	@Deprecated
	public void update(SeriexDB db) {
		db.updateStruct(this);
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
		builder.append("StructPlayer [player_id=").append(player_id).append(", api_access=").append(api_access).append(", username=").append(username).append(", password=").append(password)
					.append(", token=").append(token).append(", gAuth=").append(gAuth).append(", salt=").append(salt).append(", banned=").append(banned).append(", firstLogin=").append(firstLogin)
					.append(", timesLogined=").append(timesLogined).append(", playTime=").append(playTime).append("]");
		return builder.toString();
	}
}
