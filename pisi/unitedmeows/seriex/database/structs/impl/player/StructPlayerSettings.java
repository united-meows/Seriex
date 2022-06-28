package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "player_settings")
public class StructPlayerSettings implements IStruct {
	@Column
	public int player_settings_id;
	@Column
	public int player_id;
	@Column
	public boolean guest;
	@Column
	public boolean flags;
	@Column
	public boolean hunger;
	@Column
	public boolean fall_damage;
	@Column
	public String anticheat;
	@Column
	public String selectedLanguage;

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
		builder.append("StructPlayerSettings [player_settings_id=").append(player_settings_id).append(", player_id=").append(player_id).append(", guest=").append(guest).append(", flags=").append(flags)
					.append(", hunger=").append(hunger).append(", fall_damage=").append(fall_damage).append(", anticheat=").append(anticheat).append(", selectedLanguage=").append(selectedLanguage)
					.append("]");
		return builder.toString();
	}
}
