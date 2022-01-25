package pisi.unitedmeows.seriex.config.impl;

import pisi.unitedmeows.seriex.config.Constants;
import stelix.xfile.attributes.SxfField;
import stelix.xfile.attributes.SxfObject;

@SxfObject(name = "world_config")
public class PerWorldConfig {

	@SxfField(name = "dura_Limit")
	public int duraLimit = Constants.DEFAULT_DURA_LIMIT;

	@SxfField(name = "dura_patch")
	public boolean duraPatch = Constants.ENABLE_DURA_PATCH;

}
