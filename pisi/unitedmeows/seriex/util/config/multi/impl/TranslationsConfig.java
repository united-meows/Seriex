package pisi.unitedmeows.seriex.util.config.multi.impl;

import java.util.ArrayList;
import java.util.Arrays;

import pisi.unitedmeows.seriex.util.config.multi.MultiConfig;
import pisi.unitedmeows.seriex.util.config.multi.util.ConfigHandler;
import pisi.unitedmeows.seriex.util.config.multi.util.MultiConfigHandler;
import pisi.unitedmeows.seriex.util.config.single.util.ConfigValue;
import pisi.unitedmeows.seriex.util.config.util.Cfg;
import pisi.unitedmeows.seriex.util.language.Language;
import pisi.unitedmeows.seriex.util.language.Messages;

@Cfg(name = "Translations")
public class TranslationsConfig extends MultiConfig {
	@ConfigHandler(start = true) public MultiConfigHandler handler = () -> Arrays.stream(Language.values()).map(Language::languageCode).toList();

	public TranslationsConfig() {
		extra = new ArrayList<>();

		Arrays.stream(Messages.values()).forEach(message -> {
			extra.add(new ConfigValue<>(
						message.cfgString, // cfg key
						" default value => " + message.defaultValue, // comment
						message.defaultValue) // value
			);
		});
	}
} 
