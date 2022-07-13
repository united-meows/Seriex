package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
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
	public long linkMS;
	@Column
	public String joinedAs;
	@Column
	public int languages;

	@Override
	public String[] getColumns() {
		return DatabaseReflection.getColumnsFromClass(this.getClass()).item1();
	}

	@Override
	public YSQLCommand[] setColumns() {
		return DatabaseReflection.setAndGetColumns(this.getClass());
	}

	@Override
	public boolean create() {
		return Seriex.get().database().createStruct(this);
	}

	@Override
	public boolean update() {
		return Seriex.get().database().updateStruct(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StructPlayerDiscord [player_discord_id=").append(player_discord_id).append(", player_id=").append(player_id).append(", discord_id=").append(discord_id).append(", linkMS=")
					.append(linkMS).append(", joinedAs=").append(joinedAs).append(", languages=").append(languages).append("]");
		return builder.toString();
	}
}
