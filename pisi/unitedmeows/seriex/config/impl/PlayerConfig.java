package pisi.unitedmeows.seriex.config.impl;

import pisi.unitedmeows.yystal.utils.CoID;
import stelix.xfile.attributes.SxfField;
import stelix.xfile.attributes.SxfObject;
import stelix.xfile.writer.SxfWriter;

import java.util.List;

@SxfObject(name = "player")
public class PlayerConfig {

	@SxfField(name = "username")
	public String username;

	@SxfField(name = "token")
	public String token;

	/* hashed version of the ip */
	@SxfField(name = "address")
	public String address;


	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("PlayerConfig{");
		sb.append("username='").append(username).append('\'');
		sb.append(", token='").append(token).append('\'');
		sb.append(", address='").append(address).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
