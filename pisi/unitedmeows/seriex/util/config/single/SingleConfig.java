package pisi.unitedmeows.seriex.util.config.single;

import java.io.File;
import java.util.Locale;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.IConfig;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;

public class SingleConfig implements IConfig, ICleanup {
	private String cfgName;
	private File cfgFile;
	private CommentedFileConfig config;

	public SingleConfig() {
		this.cfgName = this.getClass().getAnnotation(Cfg.class).name().toLowerCase(Locale.ENGLISH);
		this.cfgFile = new File(parentDirectory(), cfgName + FileManager.EXTENSION);
		this.config = CommentedFileConfig.builder(cfgFile).autosave().build();
		this.config.load();
	}

	public CommentedFileConfig config() {
		return config;
	}

	@Override
	public String name() {
		return cfgName;
	}

	@Override
	public void cleanup() throws SeriexException {
		this.config.save();
		this.config.close();
	}
}
