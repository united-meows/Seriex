package pisi.unitedmeows.seriex.config.impl;

import stelix.xfile.attributes.SxfField;
import stelix.xfile.attributes.SxfObject;

@SxfObject(name = "server")
public class ServerConfig {


	@SxfField(name = "guild_id")
	public String guildId = "824637279824773231";

	@SxfField(name = "server_name")
	public String serverName = "";

	@SxfField(name = "bot_token")
	public String botToken = "INSERT_TOKEN_HERE";

	@SxfField(name = "allowC17")
	public boolean allowC17 = true;

	@SxfField(name = "c17_message")
	public String c17Message = "";

	@SxfField(name = "log")
	public boolean log = true;


	public String guildId() {
		return guildId;
	}

	public String botToken() {
		return botToken;
	}

	public boolean allowC17() {
		return allowC17;
	}

	public boolean log() {
		return log;
	}

	public String c17Message() {
		return c17Message;
	}

	public String serverName() {
		return serverName;
	}
}
