package pisi.unitedmeows.seriex.util.config.multi;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import dev.derklaro.reflexion.Reflexion;
import dev.derklaro.reflexion.matcher.FieldMatcher;
import pisi.unitedmeows.seriex.Seriex;
import pisi.unitedmeows.seriex.util.ICleanup;
import pisi.unitedmeows.seriex.util.Pair;
import pisi.unitedmeows.seriex.util.config.FileManager;
import pisi.unitedmeows.seriex.util.config.IConfig;
import pisi.unitedmeows.seriex.util.config.multi.util.MultiConfigHandler;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.config.util.ConfigField;
import pisi.unitedmeows.seriex.util.exceptions.SeriexException;
import pisi.unitedmeows.seriex.util.safety.Try;
import pisi.unitedmeows.yystal.utils.kThread;

@SuppressWarnings("resource")
public class MultiConfig implements IConfig, ICleanup {
	private String cfgName;
	private Map<String, Pair<File, CommentedFileConfig>> configs;
	private File configDirectory;
	protected List<ConfigValue<?>> extra;

	public MultiConfig() {
		this.cfgName = this.getClass().getAnnotation(Cfg.class).name().toLowerCase(Locale.ENGLISH);
		this.configs = new HashMap<>();
		File folder = new File(parentDirectory(), cfgName);
		if (!folder.exists()) {
			folder.mkdir();
		}
		this.configDirectory = folder;
	}

	public File configDirectory() {
		return configDirectory;
	}

	public void initializeSingleCfg(File configDirectory, String cfgName, MultiConfig instance) {
		try {
			File configFile = new File(configDirectory, cfgName + FileManager.EXTENSION);
			CommentedFileConfig config = CommentedFileConfig.builder(configFile)
						.concurrent()
						.sync()
						.build();
			config.load();
			configs.put(cfgName, Pair.of(configFile, config));
			boolean setup = Files.readAllBytes(configFile.toPath()).length <= 8;
			final long sleep_time = 10L;
			
			if (extra != null) {
				extra.forEach((ConfigValue<?> cfgValue) -> {
					cfgValue.config(config);
					if (setup) {
						cfgValue.value();
						kThread.sleep(sleep_time);
					}
				});
			}

			Reflexion.on(instance).findFields(FieldMatcher.newMatcher().and(f -> f.isAnnotationPresent(ConfigField.class))).forEach(accessor -> {
				ConfigValue<?> configValue = accessor.<ConfigValue<?>>getValue(instance).get();
				configValue.config(config);
				if (setup) {
					configValue.value();
					kThread.sleep(sleep_time);
				}
			});
		}
		catch (Exception e) {
			Seriex.get().logger().error("Couldnt initialize config {} - {}", cfgName, e.getLocalizedMessage());
		}
	}

	public void initialize(MultiConfigHandler handler, MultiConfig instance) {
		if (handler == null || handler.compute() == null)
			return;
		List<String> compute = handler.compute();
		if (compute.isEmpty())
			return;

		for (String cfg : compute) {
			this.initializeSingleCfg(configDirectory(), cfg, instance);
		}
	}

	public <X> X get(String subCfgName, ConfigValue<?> configValue) {
		return get(subCfgName, configValue.key());
	}

	public <X> X get(String subCfgName, String path) {
		return configs.get(subCfgName).value().get(path);
	}

	public <X> void set(String subCfgName, String path, X object) {
		CommentedFileConfig cfg = configs.get(subCfgName).value();
		cfg.set(path, object);
		cfg.save();
	}

	public <X> void set(String subCfgName, ConfigValue<X> configValue, X object) {
		CommentedFileConfig cfg = configs.get(subCfgName).value();
		cfg.set(configValue.key(), object);
		cfg.save();
	}

	public File configFile(String subCfgName) {
		return configs.get(subCfgName).key();
	}

	public Map<String, Pair<File, CommentedFileConfig>> configs() {
		return configs;
	}

	@Override
	public String name() {
		return cfgName;
	}

	@Override
	public void cleanup() throws SeriexException {
		for (Pair<File, CommentedFileConfig> pair : configs.values()) {
			CommentedFileConfig cfg = pair.value();
			Try.silent(() -> {
				cfg.save();
				cfg.close();
			});
		}
	}
}
