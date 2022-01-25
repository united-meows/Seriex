package pisi.unitedmeows.seriex.config;

import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.yystal.file.YFile;
import stelix.xfile.reader.SxfReader;
import stelix.xfile.writer.SxfWriter;

import java.io.File;

public class StelixDataProvider implements IDataProvider
{
	private static final SxfWriter writer = new SxfWriter();

	static {
		writer.setWriteType(SxfWriter.WriteType.MULTI_LINE);
	}

	@Override
	public PlayerConfig playerConfig(String username) {

		File file = ConfigManager.playerConfig(username);
		if (!file.exists()) {
			return null;
		}

		return SxfReader.readObject(PlayerConfig.class, new YFile(file).readAllText());
	}

	@Override
	public void createPlayerConfig(String username, PlayerConfig config) {
		writer.writeClassToFile(config, ConfigManager.playerConfig(username));
	}

	@Override
	public void deletePlayerConfig(String username) {
		File file = ConfigManager.playerConfig(username);
		if (file.exists()) {
			file.delete();
		}
	}

}
