package pisi.unitedmeows.seriex.util.config.impl;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * @apiNote keys are TOML keys.
 */
public class ConfigValue<X> {
	private String key;
	private Object value;
	private Config base;

	public ConfigValue(Config config, String key, X value) {
		this.key = key;
		this.value = value;
		this.base = config;
	}

	public String key() {
		return key;
	}

	public X value() {
		return base.getValue(key, base.config);
	}

	public ConfigValue<X> value(X value, CommentedConfig config) {
		this.value = value;
		base.setValue(this.key, this.value, config);
		base.save();
		return this;
	}
}
