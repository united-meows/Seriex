package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;

@Struct(name = "player_wallet")
public class StructPlayerWallet implements IStruct {
	@Column(primaryKey = true) public int player_coin_id;
	@Column(discriminator = true) public int player_id;
	@Column public int coins;
	@Column public String player_wallet;

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
		return new StringBuilder()
					.append("StructPlayerWallet [player_coin_id=").append(player_coin_id)
					.append(", player_id=").append(player_id)
					.append(", coins=").append(coins)
					.append(", player_wallet=").append(player_wallet).append("]").toString();
	}
}
