package pisi.unitedmeows.seriex.util.config.impl;

/**
 * @apiNote keys are TOML keys.
 */
public class ConfigValue {
	private String key;
	private Object value;
	private Config base;

	public ConfigValue(Config config, String key, Object value) {
		this.key = key;
		this.value = value;
		this.base = config;
	}

	public String key() {
		return key;
	}

	public Object value() {
		return value;
	}

	public ConfigValue value(Object value) {
		this.value = value;
		base.setValue(this.key, this.value);
		base.save();
		return this;
	}
}
