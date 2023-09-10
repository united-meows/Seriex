package pisi.unitedmeows.seriex.database.structs.impl.player;


import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;

@Struct(name = "player_login")
public class StructPlayerLogin implements IStruct {
	@Column(primaryKey = true) public int player_login_id;
	@Column(discriminator = true) public int player_id;
	@Column public String ip_address;
	@Column public long ms;

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
					.append("StructPlayerLogin [player_login_id=").append(player_login_id)
					.append(", player_id=").append(player_id)
					.append(", ip_address=").append(ip_address)
					.append(", login_ms=").append(ms).append("]").toString();
	}
}
