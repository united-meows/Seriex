package pisi.unitedmeows.seriex.database.structs.impl.player;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.database.structs.IStruct;
import pisi.unitedmeows.seriex.database.util.annotation.Column;
import pisi.unitedmeows.seriex.database.util.annotation.Struct;

@Struct(name = "player_discord")
public class StructPlayerDiscord implements IStruct {
	@Column(primaryKey = true) public int player_discord_id;
	@Column(discriminator = true) public int player_id;
	@Column public long snowflake;
	@Column public long linkMS;
	@Column public String joinedAs;
	@Column public int languages;

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
					.append("StructPlayerDiscord [player_discord_id=").append(player_discord_id)
					.append(", player_id=").append(player_id)
					.append(", snowflake=").append(snowflake)
					.append(", linkMS=").append(linkMS)
					.append(", joinedAs=").append(joinedAs)
					.append(", languages=").append(languages).append("]")
					.toString();
	}

	public User user() {
		return Seriex.get().discordBot().JDA()
					.retrieveUserById(this.snowflake)
					.useCache(true).complete();
	}
}
