package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.SeriexDB;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;

@Struct(name = "player")
public class StructPlayer implements IStruct {
	@Column(primaryKey = true , discriminator = true) public int player_id;
	@Column public int api_access;
	@Column public String username;
	@Column public String password;
	@Column public String token;
	@Column public String salt;
	@Column public String recovery_key;
	@Column public boolean has2FA;
	@Column public boolean banned;
	@Column public long playTime;
	@Column public int loginCounter;
	@Column public String rank_name;

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
					.append("StructPlayer [player_id=").append(player_id)
					.append(", api_access=").append(api_access)
					.append(", username=").append(username)
					.append(", password=").append(password)
					.append(", token=").append(token)
					.append(", has2FA=").append(has2FA)
					.append(", salt=").append(salt)
					.append(", recovery_key=").append(recovery_key)
					.append(", banned=").append(banned)
					.append(", loginCounter=").append(loginCounter)
					.append(", playTime=").append(playTime).append("]").toString();
	}
}
