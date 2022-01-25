package pisi.unitedmeows.seriex.config.impl;

import org.bukkit.Bukkit;
import org.bukkit.World;
import pisi.unitedmeows.seriex.config.ConfigManager;
import pisi.unitedmeows.yystal.file.YDirectory;
import pisi.unitedmeows.yystal.file.YFile;
import pisi.unitedmeows.yystal.parallel.Async;
import stelix.xfile.attributes.SxfField;
import stelix.xfile.attributes.SxfObject;
import stelix.xfile.reader.SxfReader;
import stelix.xfile.writer.SxfWriter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldConfigs {

	private YDirectory directory;

	private Map<World, PerWorldConfig> worldConfigs;

	public WorldConfigs() {
		worldConfigs = new HashMap<>();

		directory = new YDirectory(new File(ConfigManager.seriexDir(), "world_settings"));
		if (!directory.raw().exists()) {
			directory.raw().mkdirs();
		}

		SxfWriter sxfWriter = new SxfWriter();
		sxfWriter.setWriteType(SxfWriter.WriteType.MULTI_LINE);

		List<YFile> files = directory.listFiles();
		for (World world : Bukkit.getWorlds()) {
			final File worldConfigFile = new File(directory.raw(), world.getName() + ".sxf");
			if (!worldConfigFile.exists()) {
				final PerWorldConfig perWorldConfig = new PerWorldConfig();
				worldConfigs.put(world, perWorldConfig);
				Async.async(() -> sxfWriter.writeClassToFile(perWorldConfig, worldConfigFile));
			} else {
				final PerWorldConfig perWorldConfig = SxfReader.readObject(PerWorldConfig.class,
						new YFile(worldConfigFile).readAllText());
				worldConfigs.put(world, perWorldConfig);
			}

			files.removeIf(x -> x.file().getName().equals(world.getName() + ".sxf"));
		}

		/* delete non existent world configs */
		Async.async(() -> files.forEach(YFile::delete));
	}

	public void save() {
		SxfWriter sxfWriter = new SxfWriter();
		sxfWriter.setWriteType(SxfWriter.WriteType.MULTI_LINE);
		for (Map.Entry<World, PerWorldConfig> entry : worldConfigs.entrySet()) {
			final File worldConfigFile = new File(directory.raw(), entry.getKey().getName() + ".sxf");
			sxfWriter.writeClassToFile(entry.getValue(), worldConfigFile);
		}
	}

	public Map<World, PerWorldConfig> worldConfigs() {
		return worldConfigs;
	}

	public YDirectory directory() {
		return directory;
	}
}
