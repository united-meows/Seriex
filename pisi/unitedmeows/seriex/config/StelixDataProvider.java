package pisi.unitedmeows.seriex.config;

import java.io.File;
import java.nio.file.Files;

import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.config.impl.PlayerConfig;
import pisi.unitedmeows.yystal.file.YFile;
import stelix.xfile.reader.SxfReader;
import stelix.xfile.writer.SxfWriter;

public class StelixDataProvider implements IDataProvider {
	private static final SxfWriter writer = new SxfWriter();
	static {
		writer.setWriteType(SxfWriter.WriteType.MULTI_LINE);
	}

	@Override
	public PlayerConfig playerConfig(final String username) {
		final File file = ConfigManager.playerConfig(username);
		if (!file.exists()) return null;
		return SxfReader.readObject(PlayerConfig.class, new YFile(file).readAllText());
	}

	@Override
	public void createPlayerConfig(final String username, final PlayerConfig config) {
		writer.writeClassToFile(config, ConfigManager.playerConfig(username));
	}

	@Override
	public void deletePlayerConfig(final String username) {
		final File file = ConfigManager.playerConfig(username);
		if (file.exists()) {
			//			Try.safe(object -> {
			//				file.delete();
			//			}).
			/* thanks java */
			try {
				Files.delete(file.toPath());
			} catch (final Exception ignored) {
				System.gc(); // Partially closes un-closed writers & readers.
				try {
					Files.delete(file.toPath()); // trying to delete again
				} catch (final Exception realException) {
					// ah fuck
					Seriex._self.logger().fatalf("Couldnt delete %s`s player config! (exception %s)",
								username, realException.getMessage());
				}
			}
		}
	}
}
