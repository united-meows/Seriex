package pisi.unitedmeows.seriex.database.structs.impl.player;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;

@Struct(name = "player_settings")
public class StructPlayerSettings implements IStruct {
	@Column(primaryKey = true) public int player_settings_id;
	@Column(discriminator = true) public int player_id;
	@Column public boolean guest;
	@Column public boolean flags;
	@Column public boolean hunger;
	@Column public boolean fall_damage;
	@Column public String anticheat;
	@Column public String selectedLanguage;

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
					.append("StructPlayerSettings [player_settings_id=").append(player_settings_id)
					.append(", player_id=").append(player_id)
					.append(", guest=").append(guest)
					.append(", flags=").append(flags)
					.append(", hunger=").append(hunger)
					.append(", fall_damage=").append(fall_damage)
					.append(", anticheat=").append(anticheat)
					.append(", selectedLanguage=").append(selectedLanguage)
					.append("]").toString();
	}
}
