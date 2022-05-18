package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.DatabaseReflection;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;
import pisi.unitedmeows.yystal.sql.YSQLCommand;

@Struct(name = "player_wallet")
public class StructPlayerWallet implements IStruct {

	@Column
	public int player_coin_id;
	@Column
	public int player_id;
	@Column
	public int coins;
	@Column
	public String player_wallet;

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
		return String.format("StructPlayerWallet [player_coin_id=%s, player_id=%s, coins=%s, player_wallet=%s]", player_coin_id, player_id, coins, player_wallet);
	}
}
