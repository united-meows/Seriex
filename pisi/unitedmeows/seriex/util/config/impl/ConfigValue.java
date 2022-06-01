package pisi.unitedmeows.seriex.util.config.impl;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * @apiNote keys are TOML keys.
 */
public class ConfigValue<X> {
	private String key;
	private X value;
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
		X calculatedValue = value(base.config);
		if (calculatedValue == null) return value;
		return calculatedValue;
	}

	public X value(CommentedConfig config) {
		return base.getValue(key, config);
	}

	public ConfigValue<X> value(X value, CommentedConfig config) {
		this.value = value;
		base.setValue(this.key, this.value, config);
		base.save();
		return this;
	}
}
